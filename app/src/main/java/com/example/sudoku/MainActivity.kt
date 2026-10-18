package com.example.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.sudoku.data.AppContainer
import com.example.sudoku.ui.SudokuApp
import com.example.sudoku.ui.SudokuViewModel
import com.example.sudoku.ui.theme.SudokuTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SudokuViewModel by viewModels {
        SudokuViewModel.Factory(AppContainer(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SudokuTheme { SudokuApp(viewModel) } }
    }
}
