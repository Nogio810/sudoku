package com.example.sudoku.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.sudoku.domain.model.GameSession

data class GameActions(
    val select: (Int) -> Unit,
    val enter: (Int) -> Unit,
    val erase: () -> Unit,
    val toggleNotes: () -> Unit,
    val undo: () -> Unit,
    val redo: () -> Unit,
    val enterNumber: (Int) -> Unit,
)

@Composable
fun GameControls(session: GameSession, actions: GameActions, modifier: Modifier = Modifier) {
    val editable = session.selectedCell?.let { session.puzzle.givens[it] == 0 } == true && !session.isComplete
    val counts = remember(session.board.values) { session.board.values.groupingBy { it }.eachCount() }
    Box(modifier) {
        NumberPad(session.noteMode, editable, actions.enter, actions.enterNumber, counts)
    }
}
