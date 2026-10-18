package com.example.sudoku.domain.model

import com.example.sudoku.domain.game.SudokuRules

/** Stable ID and evaluator version survive changes to the source of puzzles. */
data class Puzzle(
    val id: String,
    val difficulty: Difficulty,
    val givens: List<Int>,
    val solution: List<Int>,
    val evaluatorVersion: String,
) {
    init {
        require(id.isNotBlank() && evaluatorVersion.isNotBlank())
        require(givens.size == 81 && givens.all { it in 0..9 })
        require(solution.size == 81 && solution.all { it in 1..9 })
        require(SudokuRules.conflicts(solution).isEmpty())
        require(givens.indices.all { givens[it] == 0 || givens[it] == solution[it] })
    }
}
