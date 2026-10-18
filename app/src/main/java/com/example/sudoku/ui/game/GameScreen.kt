package com.example.sudoku.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sudoku.domain.game.SudokuRules
import com.example.sudoku.domain.model.GameSession
import com.example.sudoku.ui.components.GameActions
import com.example.sudoku.ui.components.GameControls
import com.example.sudoku.ui.components.SudokuBoard
import com.example.sudoku.ui.components.formatTime
import com.example.sudoku.ui.components.GameToolbar
import com.example.sudoku.ui.components.LabeledIconButton
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import com.example.sudoku.R

@Composable
fun GameScreen(session: GameSession, actions: GameActions, onHome: () -> Unit) {
    val conflicts = remember(session.board.values) { SudokuRules.conflicts(session.board.values) }
    val homeWidth = (56.dp * LocalDensity.current.fontScale).coerceIn(64.dp, 112.dp)
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledIconButton(R.drawable.ic_home, "ホーム", "ホームに戻る", onHome,
                Modifier.width(homeWidth).testTag("home"))
            Column(Modifier.weight(1f)) {
                Text(session.puzzle.difficulty.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("経過 ${formatTime(session.elapsedSeconds)}", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(if (session.isComplete) "クリア" else "あと${session.remainingCells}マス",
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
            GameToolbar(session, actions, Modifier.widthIn(max = 630.dp).fillMaxWidth())
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            if (maxWidth >= 600.dp && maxHeight < 320.dp) {
                Row(
                    Modifier.fillMaxSize().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    BoardArea(session, conflicts, actions.select, Modifier.weight(1f).fillMaxHeight())
                    GameControls(session, actions, Modifier.weight(1f))
                }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BoardArea(session, conflicts, actions.select, Modifier.weight(1f, fill = false).fillMaxWidth())
                    GameControls(session, actions, Modifier.widthIn(max = 630.dp).fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun BoardArea(session: GameSession, conflicts: Set<Int>, onSelect: (Int) -> Unit, modifier: Modifier) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, maxHeight, 630.dp)
        SudokuBoard(session, conflicts, onSelect, Modifier.size(side))
    }
}
