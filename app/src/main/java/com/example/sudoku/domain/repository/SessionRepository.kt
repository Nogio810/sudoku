package com.example.sudoku.domain.repository

import com.example.sudoku.domain.model.GameSession

data class LocalProgress(
    val session: GameSession? = null,
    val playedPuzzleIds: Set<String> = emptySet(),
)

interface SessionRepository {
    suspend fun load(): LocalProgress
    suspend fun save(progress: LocalProgress)
}
