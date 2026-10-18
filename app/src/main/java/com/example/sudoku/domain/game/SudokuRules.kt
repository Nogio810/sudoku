package com.example.sudoku.domain.game

object SudokuRules {
    private val units: List<List<Int>> = buildList {
        for (row in 0..8) add((0..8).map { row * 9 + it })
        for (column in 0..8) add((0..8).map { it * 9 + column })
        for (block in 0..8) add((0..8).map {
            (block / 3 * 3 + it / 3) * 9 + block % 3 * 3 + it % 3
        })
    }

    /** The 20 other cells sharing a row, column, or block with this cell. */
    fun peersOf(cell: Int): Set<Int> {
        require(cell in 0..80)
        return units.filter { cell in it }.flatten().toSet() - cell
    }

    /** Only Sudoku duplicates are errors. Never compare a player's input to the answer. */
    fun conflicts(values: List<Int>): Set<Int> = buildSet {
        require(values.size == 81)
        for (unit in units) {
            unit.filter { values[it] != 0 }.groupBy { values[it] }.values
                .filter { it.size > 1 }.forEach { addAll(it) }
        }
    }

    fun isComplete(values: List<Int>): Boolean =
        values.size == 81 && values.all { it in 1..9 } && conflicts(values).isEmpty()
}
