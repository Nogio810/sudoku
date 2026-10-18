package com.example.sudoku.domain.game

import com.example.sudoku.domain.model.BoardSnapshot
import com.example.sudoku.domain.model.GameSession

/** Pure game operations. No Android, disk, UI or puzzle-generation dependencies. */
object GameEngine {
    const val HISTORY_LIMIT = 200

    fun enter(session: GameSession, digit: Int, forceNumber: Boolean = false): GameSession {
        require(digit in 1..9)
        val cell = editableCell(session) ?: return session
        val values = session.board.values.toMutableList()
        val notes = session.board.notes.toMutableList()
        val excluded = session.board.excludedCandidates.toMutableList()
        if (session.noteMode && !forceNumber) {
            if (values[cell] != 0) return session
            if (session.autoCandidates) {
                if (digit !in CandidateCalculator.legal(values, cell)) return session
                excluded[cell] = excluded[cell].let { if (digit in it) it - digit else it + digit }
            } else {
                notes[cell] = notes[cell].let { if (digit in it) it - digit else it + digit }
            }
        } else {
            if (values[cell] == digit) return session
            values[cell] = digit
            notes[cell] = emptySet()
            excluded[cell] = emptySet()
        }
        return commit(session, BoardSnapshot(values, notes, excluded))
    }

    fun erase(session: GameSession): GameSession {
        val cell = editableCell(session) ?: return session
        val values = session.board.values.toMutableList().apply { this[cell] = 0 }
        val notes = session.board.notes.toMutableList().apply { this[cell] = emptySet() }
        // On an empty automatic-candidate cell, the eraser restores only this cell's exclusions.
        val excluded = session.board.excludedCandidates.toMutableList().apply { this[cell] = emptySet() }
        return commit(session, BoardSnapshot(values, notes, excluded))
    }

    fun undo(session: GameSession): GameSession {
        if (session.isComplete || session.undo.isEmpty()) return session
        return session.copy(
            board = session.undo.last(), undo = session.undo.dropLast(1),
            redo = (session.redo + session.board).takeLast(HISTORY_LIMIT),
        )
    }

    fun redo(session: GameSession): GameSession {
        if (session.isComplete || session.redo.isEmpty()) return session
        return session.copy(
            board = session.redo.last(), redo = session.redo.dropLast(1),
            undo = (session.undo + session.board).takeLast(HISTORY_LIMIT),
        )
    }

    fun finishIfComplete(session: GameSession, now: Long = System.currentTimeMillis()): GameSession =
        if (!session.isComplete && SudokuRules.isComplete(session.board.values)) {
            session.copy(completedAtEpochMillis = now)
        } else session

    private fun editableCell(session: GameSession): Int? = session.selectedCell?.takeIf {
        !session.isComplete && it in 0..80 && session.puzzle.givens[it] == 0
    }

    private fun commit(session: GameSession, board: BoardSnapshot): GameSession =
        if (board == session.board) session else session.copy(
            board = board, undo = (session.undo + session.board).takeLast(HISTORY_LIMIT),
            redo = emptyList(),
        )
}
