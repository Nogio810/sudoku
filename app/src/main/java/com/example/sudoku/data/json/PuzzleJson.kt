package com.example.sudoku.data.json

import com.example.sudoku.domain.model.Difficulty
import com.example.sudoku.domain.model.Puzzle
import org.json.JSONArray
import org.json.JSONObject

/** Mapping of the Python generator's puzzle/solution/rating format. */
object PuzzleJson {
    fun decode(json: JSONObject): Puzzle {
        val rating = json.getJSONObject("rating")
        require(rating.getString("status") == "rated") { "Unrated puzzle cannot be offered" }
        return Puzzle(
            id = json.getString("id"),
            difficulty = Difficulty.fromLevel(rating.getInt("level")),
            givens = grid(json.getJSONArray("puzzle")),
            solution = grid(json.getJSONArray("solution")),
            evaluatorVersion = rating.getString("evaluator_version"),
        )
    }

    fun encode(puzzle: Puzzle): JSONObject = JSONObject().apply {
        put("id", puzzle.id)
        put("puzzle", gridJson(puzzle.givens))
        put("solution", gridJson(puzzle.solution))
        put("rating", JSONObject().apply {
            put("status", "rated")
            put("level", puzzle.difficulty.level)
            put("evaluator_version", puzzle.evaluatorVersion)
        })
    }

    private fun grid(array: JSONArray): List<Int> {
        require(array.length() == 9)
        return (0..8).flatMap { row ->
            val cells = array.getJSONArray(row)
            require(cells.length() == 9)
            (0..8).map { cells.getInt(it) }
        }
    }

    private fun gridJson(values: List<Int>): JSONArray = JSONArray().apply {
        values.chunked(9).forEach { put(JSONArray(it)) }
    }
}
