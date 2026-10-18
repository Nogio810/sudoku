package com.example.sudoku.ui.components

import java.util.Locale

fun formatTime(seconds: Long): String = if (seconds >= 3600) {
    String.format(Locale.JAPAN, "%d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
} else {
    String.format(Locale.JAPAN, "%02d:%02d", seconds / 60, seconds % 60)
}
