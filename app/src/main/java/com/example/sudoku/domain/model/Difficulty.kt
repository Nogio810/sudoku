package com.example.sudoku.domain.model

enum class Difficulty(val level: Int, val label: String, val description: String) {
    VERY_EASY(1, "とても簡単", "まずは、気軽にひとつ"),
    EASY(2, "簡単", "ゆっくり慣れてきたら"),
    NORMAL(3, "通常", "ほどよく頭の体操"),
    HARD(4, "難しい", "じっくり考えたい日に"),
    VERY_HARD(5, "とても難しい", "手ごたえのある一問"),
    EXTREME(6, "極限", "むずかしい問題に挑戦");

    companion object {
        fun fromLevel(level: Int): Difficulty = entries.first { it.level == level }
    }
}
