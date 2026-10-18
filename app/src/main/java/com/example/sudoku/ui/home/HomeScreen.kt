package com.example.sudoku.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sudoku.domain.model.Difficulty
import com.example.sudoku.ui.SudokuUiState
import com.example.sudoku.ui.components.formatTime

@Composable
fun HomeScreen(state: SudokuUiState, onResume: () -> Unit, onChoose: (Difficulty) -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 660.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("ひと息ついて、頭の体操。", color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge)
            Text("ゆっくり数独", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("自分のペースで、一問ずつ。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            state.session?.let { session ->
                ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (session.isComplete) "前回の問題を解きました" else "続きから遊べます", fontWeight = FontWeight.Bold)
                        Text("${session.puzzle.difficulty.label}  ·  ${formatTime(session.elapsedSeconds)}")
                        if (session.autoCandidates) Text("自動候補あり", style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = onResume, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                            Text(if (session.isComplete) "解いた盤面を見る" else "続きから遊ぶ")
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("難しさを選ぶ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Difficulty.entries.forEach { difficulty ->
                val count = state.puzzles.count { it.difficulty == difficulty }
                OutlinedCard(
                    onClick = { onChoose(difficulty) }, enabled = count > 0,
                    modifier = Modifier.fillMaxWidth().testTag("difficulty_${difficulty.level}"),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Text("${difficulty.level}", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(difficulty.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(difficulty.description, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("›", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}
