package com.example.sudoku.data.json

import com.example.sudoku.domain.game.GameEngine
import com.example.sudoku.domain.game.SudokuRules
import com.example.sudoku.domain.model.BoardSnapshot
import com.example.sudoku.domain.model.GameSession
import com.example.sudoku.domain.model.Puzzle
import com.example.sudoku.domain.repository.LocalProgress
import org.json.JSONArray
import org.json.JSONObject

object SessionJson {
    fun encode(progress: LocalProgress): String = JSONObject().apply {
        put("schema_version", 2)
        put("played_puzzle_ids", JSONArray(progress.playedPuzzleIds.toList()))
        put("session", progress.session?.let { session ->
            JSONObject().apply {
                // Embed the puzzle so an app update never invalidates the active game.
                put("puzzle", PuzzleJson.encode(session.puzzle))
                put("board", encodeBoard(session.board))
                put("selected_cell", session.selectedCell)
                put("note_mode", session.noteMode)
                put("auto_candidates", session.autoCandidates)
                put("undo", encodeHistory(session.undo))
                put("redo", encodeHistory(session.redo))
                put("elapsed_seconds", session.elapsedSeconds)
                put("started_at", session.startedAtEpochMillis)
                put("completed_at", session.completedAtEpochMillis)
            }
        })
    }.toString()

    fun decode(text: String): LocalProgress {
        val root = JSONObject(text)
        val version = root.getInt("schema_version")
        require(version in 1..2) { "Unsupported save version" }
        val ids = root.getJSONArray("played_puzzle_ids")
        return LocalProgress(
            session = root.optJSONObject("session")?.let { json ->
                val puzzle = PuzzleJson.decode(json.getJSONObject("puzzle"))
                val selected = if (json.isNull("selected_cell")) null else json.getInt("selected_cell")
                require(selected == null || selected in 0..80)
                val elapsed = json.getLong("elapsed_seconds")
                require(elapsed >= 0)
                // Version 1 games keep all their handwritten notes and remain unassisted.
                val automatic = version >= 2 && json.getBoolean("auto_candidates")
                val session = GameSession(
                    puzzle = puzzle,
                    board = decodeBoard(json.getJSONObject("board"), puzzle, version, automatic),
                    selectedCell = selected,
                    noteMode = json.getBoolean("note_mode"),
                    undo = decodeHistory(json.getJSONArray("undo"), puzzle, version, automatic),
                    redo = decodeHistory(json.getJSONArray("redo"), puzzle, version, automatic),
                    elapsedSeconds = elapsed,
                    startedAtEpochMillis = json.getLong("started_at"),
                    completedAtEpochMillis = if (json.isNull("completed_at")) null else json.getLong("completed_at"),
                    autoCandidates = automatic,
                )
                require(!session.isComplete || SudokuRules.isComplete(session.board.values))
                GameEngine.finishIfComplete(session)
            },
            playedPuzzleIds = (0 until ids.length()).map { ids.getString(it) }.toSet(),
        )
    }

    private fun encodeBoard(board: BoardSnapshot): JSONObject = JSONObject().apply {
        put("values", JSONArray(board.values))
        put("notes", JSONArray().apply { board.notes.forEach { put(JSONArray(it.sorted())) } })
        put("excluded_candidates", JSONArray().apply { board.excludedCandidates.forEach { put(JSONArray(it.sorted())) } })
    }

    private fun decodeBoard(json: JSONObject, puzzle: Puzzle, version: Int, automatic: Boolean): BoardSnapshot {
        val values = json.getJSONArray("values")
        val notes = json.getJSONArray("notes")
        require(values.length() == 81 && notes.length() == 81)
        val excluded = if (version >= 2) json.getJSONArray("excluded_candidates") else null
        require(excluded == null || excluded.length() == 81)
        val board = BoardSnapshot(
            values = (0..80).map { values.getInt(it) },
            notes = (0..80).map { cell ->
                val digits = notes.getJSONArray(cell)
                (0 until digits.length()).map { digits.getInt(it) }.toSet()
            },
            excludedCandidates = (0..80).map { cell ->
                val digits = excluded?.getJSONArray(cell)
                if (digits == null) emptySet() else (0 until digits.length()).map { digits.getInt(it) }.toSet()
            },
        )
        require(board.values.all { it in 0..9 })
        require(board.notes.all { digits -> digits.all { it in 1..9 } })
        require(board.excludedCandidates.all { digits -> digits.all { it in 1..9 } })
        require(if (automatic) board.notes.all { it.isEmpty() } else board.excludedCandidates.all { it.isEmpty() })
        require((0..80).all {
            (puzzle.givens[it] == 0 || board.values[it] == puzzle.givens[it]) &&
                (board.values[it] == 0 || (board.notes[it].isEmpty() && board.excludedCandidates[it].isEmpty()))
        })
        return board
    }

    private fun encodeHistory(history: List<BoardSnapshot>): JSONArray =
        JSONArray().apply { history.forEach { put(encodeBoard(it)) } }

    private fun decodeHistory(json: JSONArray, puzzle: Puzzle, version: Int, automatic: Boolean): List<BoardSnapshot> {
        require(json.length() <= GameEngine.HISTORY_LIMIT)
        return (0 until json.length()).map { decodeBoard(json.getJSONObject(it), puzzle, version, automatic) }
    }
}
