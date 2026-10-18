package com.example.sudoku.domain

import com.example.sudoku.domain.game.CandidateCalculator
import com.example.sudoku.domain.game.GameEngine
import com.example.sudoku.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class AutomaticCandidatesTest {
    private fun game(): GameSession {
        val solution = (0..8).flatMap { row -> (0..8).map { (row * 3 + row / 3 + it) % 9 + 1 } }
        val puzzle = Puzzle("candidates", Difficulty.NORMAL, List(81) { 0 }, solution, "1.0.0")
        return GameSession(puzzle, selectedCell = 0, noteMode = true, autoCandidates = true)
    }

    @Test fun candidatesUseOnlyTheCurrentRowColumnAndBlock() {
        val values = MutableList(81) { 0 }.apply { this[1] = 1; this[9] = 2; this[10] = 3; this[40] = 4 }
        assertEquals((4..9).toSet(), CandidateCalculator.legal(values, 0))
        assertTrue(CandidateCalculator.legal(values, 1).isEmpty())
        assertEquals((1..9).toSet(), CandidateCalculator.visibleNotes(game())[0])
    }

    @Test fun manualRemovalCanBeToggledWithoutChangingTheBoard() {
        val initial = game()
        val hidden = GameEngine.enter(initial, 4)
        assertFalse(4 in CandidateCalculator.visibleNotes(hidden)[0])
        assertEquals(setOf(4), hidden.board.excludedCandidates[0])
        assertEquals(initial.board.values, hidden.board.values)
        assertEquals(initial.board, GameEngine.enter(hidden, 4).board)
    }

    @Test fun automaticRemovalsReturnAfterErasingButManualRemovalsStayHidden() {
        val hidden = GameEngine.enter(game(), 1)
        val filled = GameEngine.enter(hidden.copy(selectedCell = 1), 2, forceNumber = true)
        assertFalse(1 in CandidateCalculator.visibleNotes(filled)[0])
        assertFalse(2 in CandidateCalculator.visibleNotes(filled)[0])
        val erased = GameEngine.erase(filled)
        assertTrue(2 in CandidateCalculator.visibleNotes(erased)[0])
        assertFalse(1 in CandidateCalculator.visibleNotes(erased)[0])
        assertEquals(setOf(1), erased.board.excludedCandidates[0])
    }

    @Test fun eraserRestoresOnlySelectedCellsLegalCandidatesAndSupportsUndoRedo() {
        var session = GameEngine.enter(game(), 1)
        session = GameEngine.enter(session.copy(selectedCell = 2), 3)
        session = GameEngine.enter(session.copy(selectedCell = 1), 2, forceNumber = true)
        session = session.copy(selectedCell = 0)
        val restored = GameEngine.erase(session)
        assertTrue(1 in CandidateCalculator.visibleNotes(restored)[0])
        assertFalse(2 in CandidateCalculator.visibleNotes(restored)[0])
        assertEquals(setOf(3), restored.board.excludedCandidates[2])
        assertEquals(session.board, GameEngine.undo(restored).board)
        assertEquals(restored.board, GameEngine.redo(GameEngine.undo(restored)).board)
    }

    @Test fun blockedCandidateCannotBeAddedByTappingIt() {
        val filled = GameEngine.enter(game().copy(selectedCell = 1), 2, forceNumber = true)
        val session = filled.copy(selectedCell = 0)
        assertEquals(session, GameEngine.enter(session, 2))
        assertFalse(2 in CandidateCalculator.visibleNotes(GameEngine.erase(session))[0])
    }

    @Test fun longPressAndErasingANumberKeepAutomaticModeAndResetThatCellsExclusions() {
        val hidden = GameEngine.enter(game(), 4)
        val filled = GameEngine.enter(hidden, 7, forceNumber = true)
        assertTrue(filled.autoCandidates && filled.noteMode)
        assertTrue(CandidateCalculator.visibleNotes(filled)[0].isEmpty())
        assertTrue(filled.board.excludedCandidates[0].isEmpty())
        val erased = GameEngine.erase(filled)
        assertEquals((1..9).toSet(), CandidateCalculator.visibleNotes(erased)[0])
        assertTrue(erased.autoCandidates && erased.noteMode)
        assertEquals(filled.board, GameEngine.undo(erased).board)
    }

    @Test fun candidateUpdatesAlsoFollowUndoAndRedoOfNumberInput() {
        val initial = game().copy(selectedCell = 1)
        val entered = GameEngine.enter(initial, 3, forceNumber = true)
        assertFalse(3 in CandidateCalculator.visibleNotes(entered)[0])
        assertTrue(3 in CandidateCalculator.visibleNotes(GameEngine.undo(entered))[0])
        assertFalse(3 in CandidateCalculator.visibleNotes(GameEngine.redo(GameEngine.undo(entered)))[0])
    }
}
