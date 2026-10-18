package com.example.sudoku.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Board roles use distinct hues instead of several barely different greens. */
@Immutable
data class BoardColors(
    val related: Color,
    val sameDigit: Color,
    val selected: Color,
    val given: Color,
    val entered: Color,
    val candidate: Color,
)

val LightBoardColors = BoardColors(
    related = Color(0xFFECF0F4), sameDigit = Color(0xFFFFEDAD), selected = Color(0xFFD7E9D9),
    given = Color(0xFF202A25), entered = Color(0xFF145AC0), candidate = Color(0xFF475566),
)

val DarkBoardColors = BoardColors(
    related = Color(0xFF293643), sameDigit = Color(0xFF5B471C), selected = Color(0xFF2C513B),
    given = Color(0xFFF0F3ED), entered = Color(0xFF8CBFFF), candidate = Color(0xFFBBCBDD),
)

val LocalBoardColors = staticCompositionLocalOf { LightBoardColors }
