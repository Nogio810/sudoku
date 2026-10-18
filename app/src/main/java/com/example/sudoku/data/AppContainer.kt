package com.example.sudoku.data

import android.content.Context
import com.example.sudoku.data.repository.AssetPuzzleRepository
import com.example.sudoku.data.repository.FileSessionRepository
import com.example.sudoku.domain.repository.PuzzleRepository
import com.example.sudoku.domain.repository.SessionRepository

/** Small composition root; cloud/cache implementations can be wired here later. */
class AppContainer(context: Context) {
    val puzzles: PuzzleRepository = AssetPuzzleRepository(context.assets)
    val sessions: SessionRepository = FileSessionRepository(context.filesDir)
}
