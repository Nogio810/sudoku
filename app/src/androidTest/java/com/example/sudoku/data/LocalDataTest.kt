package com.example.sudoku.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sudoku.data.json.SessionJson
import com.example.sudoku.data.repository.AssetPuzzleRepository
import com.example.sudoku.data.repository.FileSessionRepository
import com.example.sudoku.domain.game.GameEngine
import com.example.sudoku.domain.game.CandidateCalculator
import com.example.sudoku.domain.model.Difficulty
import com.example.sudoku.domain.model.GameSession
import com.example.sudoku.domain.repository.LocalProgress
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import org.json.JSONObject

@RunWith(AndroidJUnit4::class)
class LocalDataTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun catalogHasAllSixRatedLevelsAndUniqueIds() = runBlocking {
        val catalog = AssetPuzzleRepository(context.assets).loadPuzzles()
        assertEquals(Difficulty.entries.toSet(), catalog.map { it.difficulty }.toSet())
        assertEquals(catalog.size, catalog.map { it.id }.toSet().size)
    }

    @Test fun atomicSaveRoundTripsNotesHistoryTimerAndEmbeddedPuzzle() = runBlocking {
        val puzzle = AssetPuzzleRepository(context.assets).loadPuzzles().first()
        val cell = puzzle.givens.indexOf(0)
        val initial = GameSession(puzzle, selectedCell = cell, noteMode = true, elapsedSeconds = 123)
        val edited = GameEngine.enter(initial, 5)
        val undone = GameEngine.undo(edited)
        val progress = LocalProgress(undone, setOf(puzzle.id))
        val directory = File(context.cacheDir, "save-test-${System.nanoTime()}").apply { mkdirs() }
        try {
            val repository = FileSessionRepository(directory)
            assertEquals(LocalProgress(), repository.load())
            repository.save(progress)
            val restored = FileSessionRepository(directory).load()
            assertEquals(progress, restored)
            assertEquals(edited.board, GameEngine.redo(restored.session!!).board)
            assertEquals(initial.puzzle, restored.session!!.puzzle)
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test fun corruptSaveIsReportedInsteadOfSilentlyDiscarded() {
        try {
            SessionJson.decode("{\"schema_version\":999}")
            fail("Unsupported data must not be replaced with an empty game")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun automaticModeAndDeletedCandidatesSurviveSaveAndHistoryRestoration() = runBlocking {
        val puzzle = AssetPuzzleRepository(context.assets).loadPuzzles().first()
        val cell = puzzle.givens.indexOf(0)
        val digit = CandidateCalculator.legal(puzzle.givens, cell).first()
        val initial = GameSession(puzzle, selectedCell = cell, noteMode = true, autoCandidates = true)
        val removed = GameEngine.enter(initial, digit)
        val restored = GameEngine.erase(removed)
        val progress = LocalProgress(restored, setOf(puzzle.id))
        val result = SessionJson.decode(SessionJson.encode(progress))
        assertEquals(progress, result)
        assertTrue(result.session!!.autoCandidates)
        assertEquals(removed.board, GameEngine.undo(result.session!!).board)
        assertEquals(restored.board, GameEngine.redo(GameEngine.undo(result.session!!)).board)
    }

    @Test fun versionOneSavesRetainHandwrittenNotesAndHistoryWithoutEnablingAutomaticMode() = runBlocking {
        val puzzle = AssetPuzzleRepository(context.assets).loadPuzzles().first()
        val initial = GameSession(puzzle, selectedCell = puzzle.givens.indexOf(0), noteMode = true)
        val entered = GameEngine.enter(initial, 5)
        val legacy = JSONObject(SessionJson.encode(LocalProgress(entered, setOf(puzzle.id))))
        legacy.put("schema_version", 1)
        val session = legacy.getJSONObject("session")
        session.remove("auto_candidates")
        session.getJSONObject("board").remove("excluded_candidates")
        for (key in listOf("undo", "redo")) {
            val history = session.getJSONArray(key)
            for (index in 0 until history.length()) history.getJSONObject(index).remove("excluded_candidates")
        }
        val migrated = SessionJson.decode(legacy.toString())
        assertEquals(entered, migrated.session)
        assertFalse(migrated.session!!.autoCandidates)
        assertEquals(initial.board, GameEngine.undo(migrated.session!!).board)
        assertEquals(2, JSONObject(SessionJson.encode(migrated)).getInt("schema_version"))
    }
}
