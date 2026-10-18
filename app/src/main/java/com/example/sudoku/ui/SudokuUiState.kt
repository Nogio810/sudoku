package com.example.sudoku.ui

import com.example.sudoku.domain.model.GameSession
import com.example.sudoku.domain.model.Puzzle

enum class AppScreen { HOME, GAME }

data class SudokuUiState(
    val loading: Boolean = true,
    val loadError: Boolean = false,
    val saveError: Boolean = false,
    val puzzles: List<Puzzle> = emptyList(),
    val session: GameSession? = null,
    val playedPuzzleIds: Set<String> = emptySet(),
    val screen: AppScreen = AppScreen.HOME,
)
