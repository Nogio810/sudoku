package com.example.sudoku.domain.game

import com.example.sudoku.domain.model.GameSession

/** Rule-based candidates only; never consult the solution or apply solving techniques. */
object CandidateCalculator {
    fun legal(values: List<Int>, cell: Int): Set<Int> {
        require(values.size == 81 && cell in 0..80)
        if (values[cell] != 0) return emptySet()
        val used = SudokuRules.peersOf(cell).map { values[it] }.toSet()
        return (1..9).filter { it !in used }.toSet()
    }

    fun visibleNotes(session: GameSession): List<Set<Int>> = (0..80).map { cell ->
        when {
            session.board.values[cell] != 0 -> emptySet()
            session.autoCandidates -> legal(session.board.values, cell) - session.board.excludedCandidates[cell]
            else -> session.board.notes[cell]
        }
    }
}
