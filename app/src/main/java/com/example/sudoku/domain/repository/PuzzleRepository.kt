package com.example.sudoku.domain.repository

import com.example.sudoku.domain.model.Puzzle

/** Implement a local cache + remote fetch here when the cloud service is ready. */
interface PuzzleRepository {
    suspend fun loadPuzzles(): List<Puzzle>
}
