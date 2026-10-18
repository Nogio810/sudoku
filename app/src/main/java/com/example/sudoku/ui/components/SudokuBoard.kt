package com.example.sudoku.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.domain.model.GameSession
import com.example.sudoku.domain.game.SudokuRules
import com.example.sudoku.domain.game.CandidateCalculator
import com.example.sudoku.ui.theme.LocalBoardColors
import kotlin.math.min

@Composable
fun SudokuBoard(
    session: GameSession,
    conflicts: Set<Int>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val boardColors = LocalBoardColors.current
    val visibleNotes = remember(session.board, session.autoCandidates) { CandidateCalculator.visibleNotes(session) }
    val selectedValue = session.selectedCell?.let { session.board.values[it] }?.takeIf { it != 0 }
    val relatedCells = remember(session.selectedCell) {
        session.selectedCell?.let(SudokuRules::peersOf).orEmpty()
    }
    BoxWithConstraints(modifier.aspectRatio(1f).testTag("sudoku_board")) {
        val cellSize = maxWidth / 9
        val fontScale = LocalDensity.current.fontScale
        val digitSize = min(30f, cellSize.value * 0.66f / fontScale).sp
        val noteSize = min(14f, cellSize.value * 0.29f / fontScale).sp
        Column(Modifier.fillMaxSize().background(colors.surface)) {
            repeat(9) { row ->
                Row(Modifier.weight(1f)) {
                    repeat(9) { column ->
                        val cell = row * 9 + column
                        val value = session.board.values[cell]
                        val isSelected = session.selectedCell == cell
                        val isConflict = cell in conflicts
                        val given = session.puzzle.givens[cell] != 0
                        val background = when {
                            isSelected -> boardColors.selected
                            isConflict -> colors.errorContainer
                            value != 0 && value == selectedValue -> boardColors.sameDigit
                            cell in relatedCells -> boardColors.related
                            else -> colors.surface
                        }
                        val description = buildString {
                            append("${row + 1}行${column + 1}列、")
                            append(if (value == 0) "空きマス" else "数字$value")
                            if (given) append("、最初からある数字")
                            if (isConflict) append("、数字が重複しています")
                            if (visibleNotes[cell].isNotEmpty()) {
                                append("、${if (session.autoCandidates) "候補" else "メモ"} ${visibleNotes[cell].sorted().joinToString("、")}")
                            }
                        }
                        Box(
                            Modifier.weight(1f).fillMaxHeight().background(background)
                                .testTag("cell_$cell")
                                .semantics(mergeDescendants = true) {
                                    contentDescription = description
                                    selected = isSelected
                                }
                                .clickable(role = Role.Button, onClickLabel = "このマスを選ぶ") { onSelect(cell) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (value != 0) {
                                Text(
                                    value.toString(), fontSize = digitSize,
                                    fontWeight = if (given || isConflict) FontWeight.Bold else FontWeight.Medium,
                                    color = when { isConflict -> colors.error; given -> boardColors.given; else -> boardColors.entered },
                                    textDecoration = if (isConflict) TextDecoration.Underline else null,
                                    modifier = Modifier.clearAndSetSemantics {},
                                )
                            } else {
                                CandidateNotes(visibleNotes[cell], noteSize)
                            }
                        }
                    }
                }
            }
        }
        // Draw grid lines over the cells without changing their touch targets.
        Canvas(Modifier.fillMaxSize()) {
            for (line in 0..9) {
                val major = line % 3 == 0
                val stroke = if (major) 2.dp.toPx() else 0.65.dp.toPx()
                val color = if (major) colors.onSurface else colors.outline.copy(alpha = 0.65f)
                val x = (size.width * line / 9).coerceIn(stroke / 2, size.width - stroke / 2)
                val y = (size.height * line / 9).coerceIn(stroke / 2, size.height - stroke / 2)
                drawLine(color, Offset(x, 0f), Offset(x, size.height), stroke)
                drawLine(color, Offset(0f, y), Offset(size.width, y), stroke)
            }
        }
        if (session.selectedCell != null) {
            val index = session.selectedCell
            Canvas(Modifier.fillMaxSize()) {
                val left = size.width * (index % 9) / 9
                val top = size.height * (index / 9) / 9
                drawRect(colors.primary, Offset(left + 2.dp.toPx(), top + 2.dp.toPx()),
                    androidx.compose.ui.geometry.Size(size.width / 9 - 4.dp.toPx(), size.height / 9 - 4.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
            }
        }
    }
}

@Composable
private fun CandidateNotes(notes: Set<Int>, size: androidx.compose.ui.unit.TextUnit) {
    Column(Modifier.fillMaxSize().padding(2.dp).clearAndSetSemantics {}) {
        repeat(3) { row ->
            Row(Modifier.weight(1f)) {
                repeat(3) { column ->
                    val digit = row * 3 + column + 1
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        if (digit in notes) Text(digit.toString(), fontSize = size,
                            lineHeight = size, color = LocalBoardColors.current.candidate)
                    }
                }
            }
        }
    }
}
