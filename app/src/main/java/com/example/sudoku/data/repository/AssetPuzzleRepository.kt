package com.example.sudoku.data.repository

import android.content.res.AssetManager
import com.example.sudoku.data.json.PuzzleJson
import com.example.sudoku.domain.model.Puzzle
import com.example.sudoku.domain.repository.PuzzleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AssetPuzzleRepository(private val assets: AssetManager) : PuzzleRepository {
    override suspend fun loadPuzzles(): List<Puzzle> = withContext(Dispatchers.IO) {
        val root = JSONObject(assets.open("puzzles.json").bufferedReader().use { it.readText() })
        require(root.getInt("schema_version") == 1)
        val entries = root.getJSONArray("puzzles")
        val puzzles = (0 until entries.length()).map { PuzzleJson.decode(entries.getJSONObject(it)) }
        require(puzzles.isNotEmpty())
        require(puzzles.map { it.id }.distinct().size == puzzles.size) { "Duplicate puzzle IDs" }
        puzzles
    }
}
