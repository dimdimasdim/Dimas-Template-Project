package com.dimas.dimasproject.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI architecture.
 *
 * @param S UI state type — must implement [BaseUiState]   (Model in MVI)
 * @param I User intent type — must implement [BaseIntent]  (Intent in MVI)
 * @param E UI effect type — must implement [BaseUiEffect]  (one-time side effects)
 *
 * The View layer (Compose):
 *  - Observes [uiState] for persistent screen state.
 *  - Dispatches user actions via [onIntent].
 *  - Collects [effect] inside a LaunchedEffect for one-time events
 *    (navigation, snackbar, toast, etc.).
 */
abstract class BaseViewModel<S : BaseUiState, I : BaseIntent, E : BaseUiEffect>(
    initialState: S
) : ViewModel() {

    // ── State ────────────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(initialState)

    /** Exposed immutable state stream for the View layer. */
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    /** Synchronous snapshot of the current state. */
    protected val currentState: S
        get() = _uiState.value

    // ── Effect ───────────────────────────────────────────────────────────────────

    //    private val _effect = Channel<E>(Channel.BUFFERED)
    private val _effect = MutableSharedFlow<E>(
        replay = 0, //no replay → no duplicate event
        extraBufferCapacity = 1
    )

    /**
     * One-time side-effect stream. Backed by a [Channel] so each event is
     * delivered exactly once regardless of recomposition.
     *
     * Collect this inside a LaunchedEffect in the View:
     * ```
     * LaunchedEffect(Unit) {
     *     viewModel.effect.collect { effect ->
     *         when (effect) { ... }
     *     }
     * }
     * ```
     */
//    val effect: Flow<E> = _effect.receiveAsFlow()
    val effect: SharedFlow<E> = _effect

    // ── Intent ───────────────────────────────────────────────────────────────────

    /**
     * Entry point for the View to dispatch an intent.
     * Routes to [handleIntent] which each concrete ViewModel must implement.
     */
    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    /**
     * Implement this to handle each intent and produce a new state
     * via [updateState] / [setState], or a side effect via [sendEffect].
     */
    protected abstract fun handleIntent(intent: I)

    // ── Helpers ──────────────────────────────────────────────────────────────────

    /**
     * Applies a transformation to the current state and emits the result.
     *   updateState { copy(isLoading = true) }
     */
    protected fun updateState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    /** Replaces the entire state with [newState]. */
    protected fun setState(newState: S) {
        _uiState.value = newState
    }

    /**
     * Sends a one-time [effect] through the Channel.
     * Safe to call from any coroutine context — dispatched on [viewModelScope].
     */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
//            _effect.send(effect)
            _effect.tryEmit(effect)
        }
    }

    override fun onCleared() {
        super.onCleared()
//        _effect.close()
    }
}
