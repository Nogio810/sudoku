package com.example.sudoku.domain

import com.example.sudoku.domain.game.GameEngine
import com.example.sudoku.domain.game.SudokuRules
import com.example.sudoku.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {
    private val solution = (0..8).flatMap { row -> (0..8).map { (row * 3 + row / 3 + it) % 9 + 1 } }
    private fun session(): GameSession {
        val givens = solution.toMutableList().apply { this[0] = 0; this[1] = 0; this[80] = 0 }
        return GameSession(Puzzle("test", Difficulty.VERY_EASY, givens, solution, "1.0.0"), selectedCell = 0)
    }

    @Test fun notesAndTheirReplacementAreRestoredTogether() {
        val initial = session().copy(noteMode = true)
        val withNote = GameEngine.enter(initial, 4)
        assertEquals(setOf(4), withNote.board.notes[0])
        assertEquals(0, withNote.board.values[0])
        val filled = GameEngine.enter(withNote.copy(noteMode = false), 1)
        assertTrue(filled.board.notes[0].isEmpty())
        val undone = GameEngine.undo(filled)
        assertEquals(withNote.board, undone.board)
        assertEquals(filled.board, GameEngine.redo(undone).board)
    }

    @Test fun originalNumbersCannotBeChangedOrErased() {
        val original = session().copy(selectedCell = 2)
        assertEquals(original, GameEngine.enter(original, 9))
        assertEquals(original, GameEngine.erase(original))
        assertEquals(original, GameEngine.enter(original, 9, forceNumber = true))
    }

    @Test fun longPressEntersNumberWithoutLeavingMemoModeAndCanBeUndone() {
        val memo = GameEngine.enter(session().copy(noteMode = true), 4)
        val entered = GameEngine.enter(memo, 1, forceNumber = true)
        assertEquals(1, entered.board.values[0])
        assertTrue(entered.board.notes[0].isEmpty())
        assertTrue(entered.noteMode)
        assertEquals(memo.board, GameEngine.undo(entered).board)
        assertEquals(entered.board, GameEngine.redo(GameEngine.undo(entered)).board)
        // A filled cell remains replaceable with a long press while memo mode stays on.
        assertEquals(2, GameEngine.enter(entered, 2, forceNumber = true).board.values[0])
    }

    @Test fun relatedCellsFollowSelectionAndContainOnlyItsRowColumnAndBlock() {
        for (selected in 0..80) {
            val expected = (0..80).filter { cell ->
                cell != selected && (cell / 9 == selected / 9 || cell % 9 == selected % 9 ||
                    (cell / 27 == selected / 27 && cell % 9 / 3 == selected % 9 / 3))
            }.toSet()
            assertEquals(expected, SudokuRules.peersOf(selected))
            assertEquals(20, expected.size)
        }
    }

    @Test fun replacingInputAfterUndoDiscardsRedo() {
        val entered = GameEngine.enter(session(), 3)
        val undone = GameEngine.undo(entered)
        val newInput = GameEngine.enter(undone, 4)
        assertTrue(newInput.redo.isEmpty())
        assertEquals(4, newInput.board.values[0])
    }

    @Test fun wrongAnswerWithoutDuplicateIsAllowed() {
        // 2 belongs in cell 1 but does not conflict with any remaining given at cell 0.
        val initial = session().let { game ->
            val givens = game.puzzle.givens.mapIndexed { i, value ->
                if (i / 9 == 0 || i % 9 == 0 || (i / 9 < 3 && i % 9 < 3)) 0 else value
            }
            GameSession(game.puzzle.copy(givens = givens), selectedCell = 0)
        }
        val wrong = GameEngine.enter(initial, 2)
        assertEquals(2, wrong.board.values[0])
        assertTrue(SudokuRules.conflicts(wrong.board.values).isEmpty())
        assertFalse(GameEngine.finishIfComplete(wrong).isComplete)
    }

    @Test fun duplicatesMarkBothCellsInRowsColumnsAndBlocks() {
        for (other in listOf(8, 72, 10)) {
            val values = MutableList(81) { 0 }.apply { this[0] = 5; this[other] = 5 }
            assertEquals(setOf(0, other), SudokuRules.conflicts(values))
        }
    }

    @Test fun fullButConflictingBoardDoesNotComplete() {
        val game = session()
        val wrong = game.copy(board = BoardSnapshot(solution.toMutableList().apply { this[0] = 2 }))
        assertFalse(GameEngine.finishIfComplete(wrong).isComplete)
    }

    @Test fun completedGameIsLockedAndTimestampIsStable() {
        val game = session().copy(board = BoardSnapshot(solution))
        val complete = GameEngine.finishIfComplete(game, 1234)
        assertEquals(1234L, complete.completedAtEpochMillis)
        assertEquals(complete, GameEngine.finishIfComplete(complete, 5678))
        assertEquals(complete, GameEngine.erase(complete))
    }

    @Test fun historyHasAnExplicitBound() {
        var game = session()
        repeat(250) { game = GameEngine.enter(game, it % 9 + 1) }
        assertEquals(GameEngine.HISTORY_LIMIT, game.undo.size)
    }
}
