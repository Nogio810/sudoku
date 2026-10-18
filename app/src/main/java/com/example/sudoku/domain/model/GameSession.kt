package com.example.sudoku.domain.model

data class BoardSnapshot(
    val values: List<Int>,
    val notes: List<Set<Int>> = List(81) { emptySet() },
    val excludedCandidates: List<Set<Int>> = List(81) { emptySet() },
)

/** One local game; history contains values and notes together for reliable undo. */
data class GameSession(
    val puzzle: Puzzle,
    val board: BoardSnapshot = BoardSnapshot(puzzle.givens),
    val selectedCell: Int? = null,
    val noteMode: Boolean = false,
    val undo: List<BoardSnapshot> = emptyList(),
    val redo: List<BoardSnapshot> = emptyList(),
    val elapsedSeconds: Long = 0,
    val startedAtEpochMillis: Long = System.currentTimeMillis(),
    val completedAtEpochMillis: Long? = null,
    val autoCandidates: Boolean = false,
) {
    val remainingCells: Int get() = board.values.count { it == 0 }
    val isComplete: Boolean get() = completedAtEpochMillis != null
}
