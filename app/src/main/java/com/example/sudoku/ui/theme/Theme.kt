package com.example.sudoku.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFACD4B7), onPrimary = Color(0xFF153D28),
    primaryContainer = Color(0xFF2C513B), onPrimaryContainer = Color(0xFFD1EBD6),
    secondaryContainer = Color(0xFF34483A), onSecondaryContainer = Color(0xFFE2EBDE),
    secondary = Color(0xFFC4CEBD), background = Color(0xFF171D19),
    surface = Color(0xFF1D2520), onSurface = Color(0xFFE3E9E0),
    onSurfaceVariant = Color(0xFFBCC8BE), outline = Color(0xFF8D9F91),
    outlineVariant = Color(0xFF526052),
    surfaceVariant = Color(0xFF303D33), error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF622F2B), onErrorContainer = Color(0xFFFFDAD6),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF315E43), onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E9D9), onPrimaryContainer = Color(0xFF173F28),
    secondaryContainer = Color(0xFFE2EBDE), onSecondaryContainer = Color(0xFF24442F),
    secondary = Color(0xFF52614E), background = Color(0xFFF8F7F2),
    surface = Color(0xFFFFFEF9), onSurface = Color(0xFF242F28),
    onSurfaceVariant = Color(0xFF526256), outline = Color(0xFF788A7D),
    outlineVariant = Color(0xFFC1CCC0),
    surfaceVariant = Color(0xFFE8EDE4), error = Color(0xFFA52929),
    errorContainer = Color(0xFFFBE5DF), onErrorContainer = Color(0xFF792020),
)

@Composable
fun SudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalBoardColors provides if (darkTheme) DarkBoardColors else LightBoardColors) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
