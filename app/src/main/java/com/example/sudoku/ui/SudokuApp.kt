package com.example.sudoku.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.sudoku.domain.model.Difficulty
import com.example.sudoku.ui.components.GameActions
import com.example.sudoku.ui.components.formatTime
import com.example.sudoku.ui.game.GameScreen
import com.example.sudoku.ui.home.HomeScreen
import com.example.sudoku.ui.home.NewGameDialog
import kotlinx.coroutines.delay

@Composable
fun SudokuApp(viewModel: SudokuViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDifficulty by rememberSaveable { mutableStateOf<Difficulty?>(null) }
    var dismissedCompletion by rememberSaveable { mutableStateOf<String?>(null) }
    val session = state.session
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var foreground by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            foreground = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) viewModel.persist()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); viewModel.persist() }
    }
    LaunchedEffect(foreground, state.screen, session?.puzzle?.id, session?.isComplete) {
        if (foreground && state.screen == AppScreen.GAME && session?.isComplete == false) {
            var previous = SystemClock.elapsedRealtime()
            while (true) {
                delay(1000)
                val now = SystemClock.elapsedRealtime()
                val seconds = (now - previous) / 1000
                if (seconds > 0) { viewModel.addElapsed(seconds); previous += seconds * 1000 }
            }
        }
    }
    BackHandler(state.screen == AppScreen.GAME && !state.loading) { viewModel.home() }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            if (state.saveError) {
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("保存できませんでした。保存を再試行してください。")
                        TextButton(onClick = viewModel::persist) { Text("保存を再試行") }
                    }
                }
            }
            Box(Modifier.weight(1f)) {
                when {
                    state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    state.loadError -> Column(
                        Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("問題または保存データを読み込めませんでした。")
                        Button(onClick = viewModel::reload) { Text("読み込みを再試行") }
                    }
                    state.screen == AppScreen.GAME && session != null -> GameScreen(
                        session,
                        GameActions(viewModel::selectCell, viewModel::enter, viewModel::erase,
                            viewModel::toggleNotes, viewModel::undo, viewModel::redo, viewModel::enterNumber),
                        viewModel::home,
                    )
                    else -> HomeScreen(state, viewModel::resume) { difficulty ->
                        pendingDifficulty = difficulty
                    }
                }
            }
        }
    }
    pendingDifficulty?.let { difficulty ->
        NewGameDialog(
            difficulty = difficulty, replacesCurrent = session != null && !session.isComplete,
            onDismiss = { pendingDifficulty = null },
            onStart = { automatic ->
                pendingDifficulty = null
                viewModel.startGame(difficulty, automatic)
            },
        )
    }
    val completionKey = session?.completedAtEpochMillis?.let { "${session.startedAtEpochMillis}:$it" }
    if (state.screen == AppScreen.GAME && completionKey != null && completionKey != dismissedCompletion) {
        AlertDialog(
            onDismissRequest = { dismissedCompletion = completionKey },
            title = { Text("おめでとうございます！") },
            text = { Text("一問、解けました。\n${session!!.puzzle.difficulty.label} · ${formatTime(session.elapsedSeconds)}\nおつかれさまでした。") },
            confirmButton = { TextButton(onClick = {
                dismissedCompletion = completionKey; viewModel.home()
            }) { Text("次の問題を選ぶ") } },
            dismissButton = { TextButton(onClick = { dismissedCompletion = completionKey }) { Text("盤面を見る") } },
        )
    }
}
