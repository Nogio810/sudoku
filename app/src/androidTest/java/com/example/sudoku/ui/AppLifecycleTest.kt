package com.example.sudoku.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sudoku.MainActivity
import com.example.sudoku.data.json.SessionJson
import com.example.sudoku.domain.model.Difficulty
import com.example.sudoku.domain.model.GameSession
import com.example.sudoku.domain.game.CandidateCalculator
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AppLifecycleTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun model() = ViewModelProvider(compose.activity)[SudokuViewModel::class.java]

    @Test fun inputNotesAndUndoHistorySurviveActivityRecreation() {
        compose.waitUntil(10_000) { !model().state.value.loading }
        var original: GameSession? = null
        compose.runOnIdle {
            model().startGame(Difficulty.VERY_EASY)
            val cell = model().state.value.session!!.puzzle.givens.indexOf(0)
            model().selectCell(cell)
        }
        compose.onNodeWithTag("notes").assertIsDisplayed().performClick()
        compose.onNodeWithTag("digit_5").assertIsDisplayed().performClick()
        compose.runOnIdle { original = model().state.value.session }
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "progress.json")
        compose.waitUntil(10_000) {
            runCatching { SessionJson.decode(file.readText()).session?.board == original?.board }.getOrDefault(false)
        }
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10_000) { !model().state.value.loading }
        compose.runOnIdle {
            assertEquals(original!!.board, model().state.value.session!!.board)
            assertEquals(original!!.puzzle.id, model().state.value.session!!.puzzle.id)
            assertTrue(model().state.value.session!!.noteMode)
        }
        compose.onNodeWithTag("undo").assertIsDisplayed().assertIsEnabled().performClick()
        compose.runOnIdle {
            assertTrue(model().state.value.session!!.board.notes[original!!.selectedCell!!].isEmpty())
        }
        compose.onNodeWithTag("redo").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(original!!.board, model().state.value.session!!.board) }
        compose.onNodeWithTag("digit_7").performTouchInput { longClick() }
        compose.runOnIdle {
            val session = model().state.value.session!!
            val cell = session.selectedCell!!
            assertTrue(session.noteMode)
            assertEquals(7, session.board.values[cell])
            assertTrue(session.board.notes[cell].isEmpty())
        }
        compose.waitUntil(10_000) {
            runCatching {
                val session = SessionJson.decode(file.readText()).session!!
                session.board.values[session.selectedCell!!] == 7 && session.noteMode
            }.getOrDefault(false)
        }
    }

    @Test fun completionAndReplacingUnfinishedGameFollowUserActions() {
        compose.waitUntil(10_000) { !model().state.value.loading }
        compose.runOnIdle {
            model().startGame(Difficulty.VERY_EASY)
            val puzzle = model().state.value.session!!.puzzle
            puzzle.givens.indices.filter { puzzle.givens[it] == 0 }.forEach { cell ->
                model().selectCell(cell)
                model().enter(puzzle.solution[cell])
            }
        }
        compose.onNodeWithText("次の問題を選ぶ").assertIsDisplayed().performClick()
        compose.onNodeWithTag("difficulty_2").performScrollTo().performClick()
        compose.onNodeWithTag("start_game").performClick()
        var id: String? = null
        compose.runOnIdle { id = model().state.value.session!!.puzzle.id; model().home() }
        compose.onNodeWithTag("difficulty_3").performScrollTo().performClick()
        compose.onNodeWithText("新しい問題を始めますか？").assertIsDisplayed()
        compose.onNodeWithText("戻る").performClick()
        compose.runOnIdle { assertEquals(id, model().state.value.session!!.puzzle.id) }
    }

    @Test fun automaticCandidatesAreChosenOnlyBeforeStartingAndCanBeRestoredWithEraser() {
        compose.waitUntil(10_000) { !model().state.value.loading }
        compose.runOnIdle { model().home() }
        compose.onNodeWithTag("difficulty_1").performScrollTo().performClick()
        compose.onNodeWithTag("auto_candidates").assertIsOff().performClick().assertIsOn()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("auto_candidates").assertIsOn()
        compose.onNodeWithTag("start_game").performClick()
        var cell = 0
        var digit = 0
        compose.runOnIdle {
            val session = model().state.value.session!!
            assertTrue(session.autoCandidates)
            cell = session.puzzle.givens.indexOf(0)
            digit = CandidateCalculator.legal(session.board.values, cell).first()
            model().selectCell(cell)
        }
        compose.onNodeWithTag("auto_candidates").assertDoesNotExist()
        compose.onNodeWithTag("notes").performClick()
        compose.onNodeWithTag("digit_$digit").performClick()
        compose.runOnIdle { assertEquals(setOf(digit), model().state.value.session!!.board.excludedCandidates[cell]) }
        compose.onNodeWithTag("erase").performClick()
        compose.runOnIdle { assertTrue(model().state.value.session!!.board.excludedCandidates[cell].isEmpty()) }
        compose.onNodeWithTag("undo").performClick()
        compose.runOnIdle { assertEquals(setOf(digit), model().state.value.session!!.board.excludedCandidates[cell]) }
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir, "progress.json")
        compose.waitUntil(10_000) {
            runCatching {
                val saved = SessionJson.decode(file.readText()).session!!
                saved.autoCandidates && digit in saved.board.excludedCandidates[cell]
            }.getOrDefault(false)
        }
        compose.onNodeWithTag("home").performClick()
        compose.onNodeWithTag("difficulty_2").performScrollTo().performClick()
        compose.onNodeWithTag("auto_candidates").assertIsOff()
        compose.onNodeWithText("戻る").performClick()
        compose.runOnIdle { assertTrue(model().state.value.session!!.autoCandidates) }
    }
}
