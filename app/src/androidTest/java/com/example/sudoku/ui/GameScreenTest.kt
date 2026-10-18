package com.example.sudoku.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sudoku.data.repository.AssetPuzzleRepository
import com.example.sudoku.domain.model.GameSession
import com.example.sudoku.domain.model.BoardSnapshot
import com.example.sudoku.ui.components.GameActions
import com.example.sudoku.ui.game.GameScreen
import com.example.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun allDigitsAreOnOneVisibleRowAndLongPressDoesNotTriggerShortPress() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val puzzle = runBlocking { AssetPuzzleRepository(context.assets).loadPuzzles().first() }
        val cell = puzzle.givens.indexOf(0)
        var selected: Int? = null
        var entered: Int? = null
        var number: Int? = null
        compose.setContent {
            SudokuTheme {
                GameScreen(GameSession(puzzle, selectedCell = cell, noteMode = true),
                    GameActions({ selected = it }, { entered = it }, {}, {}, {}, {}, { number = it }), {})
            }
        }
        compose.onNodeWithTag("cell_$cell").performClick()
        assertEquals(cell, selected)
        val firstTop = compose.onNodeWithTag("digit_1").fetchSemanticsNode().boundsInRoot.top
        for (digit in 1..9) {
            val key = compose.onNodeWithTag("digit_$digit").assertIsDisplayed().assertIsEnabled()
            assertEquals(firstTop, key.fetchSemanticsNode().boundsInRoot.top, 0.5f)
        }
        compose.onAllNodes(hasScrollAction()).assertCountEquals(0)
        compose.onNodeWithTag("digit_5").performClick()
        assertEquals(5, entered)
        assertNull(number)
        compose.onNodeWithTag("digit_7").performTouchInput { longClick() }
        assertEquals(7, number)
        assertEquals(5, entered)
        compose.onNodeWithTag("erase").assertIsDisplayed()
        compose.onNodeWithText("入れる数字を押してください").assertDoesNotExist()
        compose.onNodeWithText("あせらず、ゆっくりどうぞ。").assertDoesNotExist()
        compose.onNodeWithText("自動保存", substring = true).assertDoesNotExist()
    }

    @Test fun ninePlacedDigitsRemainEnabledForTentativeInput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val puzzle = runBlocking { AssetPuzzleRepository(context.assets).loadPuzzles().first() }
        val cell = puzzle.givens.indices.first { puzzle.givens[it] == 0 && puzzle.solution[it] != 1 }
        val values = puzzle.solution.toMutableList().apply { this[cell] = 0 }
        assertEquals(9, values.count { it == 1 })
        var entered: Int? = null
        compose.setContent {
            SudokuTheme {
                GameScreen(GameSession(puzzle, board = BoardSnapshot(values), selectedCell = cell),
                    GameActions({}, { entered = it }, {}, {}, {}, {}, {}), {})
            }
        }
        compose.onNodeWithTag("digit_1").assertIsDisplayed().assertIsEnabled().performClick()
        assertEquals(1, entered)
    }
}
