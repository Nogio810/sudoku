package com.example.sudoku.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sudoku.data.AppContainer
import com.example.sudoku.domain.game.GameEngine
import com.example.sudoku.domain.model.Difficulty
import com.example.sudoku.domain.model.GameSession
import com.example.sudoku.domain.repository.LocalProgress
import com.example.sudoku.domain.repository.PuzzleRepository
import com.example.sudoku.domain.repository.SessionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SudokuViewModel(
    private val puzzles: PuzzleRepository,
    private val sessions: SessionRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SudokuUiState())
    val state = mutableState.asStateFlow()
    private val writes = Channel<LocalProgress>(Channel.CONFLATED)

    init {
        viewModelScope.launch {
            for (progress in writes) {
                try {
                    sessions.save(progress)
                    mutableState.value = mutableState.value.copy(saveError = false)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    mutableState.value = mutableState.value.copy(saveError = true)
                }
            }
        }
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, loadError = false)
            try {
                val catalog = puzzles.loadPuzzles()
                val progress = sessions.load()
                mutableState.value = SudokuUiState(
                    loading = false, puzzles = catalog, session = progress.session,
                    playedPuzzleIds = progress.playedPuzzleIds,
                    screen = if (progress.session != null) AppScreen.GAME else AppScreen.HOME,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(loading = false, loadError = true)
            }
        }
    }

    fun startGame(difficulty: Difficulty, autoCandidates: Boolean = false) {
        val current = state.value
        val candidates = current.puzzles.filter { it.difficulty == difficulty }
        val unseen = candidates.filter { it.id !in current.playedPuzzleIds }
        // Avoid immediately repeating the current puzzle when the catalog permits.
        val alternatives = candidates.filter { it.id != current.session?.puzzle?.id }
        val puzzle = (unseen.ifEmpty { alternatives.ifEmpty { candidates } }).firstOrNull() ?: return
        mutableState.value = current.copy(
            session = GameSession(puzzle, autoCandidates = autoCandidates), screen = AppScreen.GAME,
            playedPuzzleIds = current.playedPuzzleIds + puzzle.id,
        )
        persist()
    }

    fun home() { mutableState.value = state.value.copy(screen = AppScreen.HOME); persist() }
    fun resume() { mutableState.value = state.value.copy(screen = AppScreen.GAME) }

    fun selectCell(index: Int) {
        if (index in 0..80) updateSession { it.copy(selectedCell = index) }
    }

    fun toggleNotes() = updateSession { if (it.isComplete) it else it.copy(noteMode = !it.noteMode) }
    fun enter(digit: Int) = updateSession { GameEngine.finishIfComplete(GameEngine.enter(it, digit)) }
    fun enterNumber(digit: Int) = updateSession {
        GameEngine.finishIfComplete(GameEngine.enter(it, digit, forceNumber = true))
    }
    fun erase() = updateSession { GameEngine.erase(it) }
    fun undo() = updateSession { GameEngine.undo(it) }
    fun redo() = updateSession { GameEngine.redo(it) }

    fun addElapsed(seconds: Long) {
        if (seconds <= 0 || state.value.screen != AppScreen.GAME) return
        updateSession(save = false) {
            if (it.isComplete) it else it.copy(elapsedSeconds = it.elapsedSeconds + seconds)
        }
        if ((state.value.session?.elapsedSeconds ?: 0) % 5L == 0L) persist()
    }

    fun persist() {
        val current = state.value
        if (!current.loading && !current.loadError) {
            writes.trySend(LocalProgress(current.session, current.playedPuzzleIds))
        }
    }

    private fun updateSession(save: Boolean = true, transform: (GameSession) -> GameSession) {
        val current = state.value
        val session = current.session ?: return
        val updated = transform(session)
        if (updated == session) return
        mutableState.value = current.copy(session = updated)
        if (save) persist()
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SudokuViewModel::class.java))
            return SudokuViewModel(container.puzzles, container.sessions) as T
        }
    }
}
