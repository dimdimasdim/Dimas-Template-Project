package com.dimas.dimasproject.core.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base ViewModel for MVI architecture.
 *
 * @param S UI state type — must implement [BaseUiState]   (Model in MVI)
 * @param I User intent type — must implement [BaseIntent]  (Intent in MVI)
 *
 * The View layer (Compose) observes [uiState] and dispatches actions via [onIntent].
 */
abstract class BaseViewModel<S : BaseUiState, I : BaseIntent>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)

    /** Exposed immutable state stream for the View layer. */
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    /** Snapshot of the current state. */
    protected val currentState: S
        get() = _uiState.value

    /**
     * Entry point for the View to dispatch an intent.
     * Routes to [handleIntent] which each screen ViewModel must implement.
     */
    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    /**
     * Must be implemented by concrete ViewModels to handle each intent
     * and produce a new state via [updateState] or [setState].
     */
    protected abstract fun handleIntent(intent: I)

    /**
     * Applies a transformation to the current state and emits the result.
     * Use this for partial state updates:
     *   updateState { copy(isLoading = true) }
     */
    protected fun updateState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    /**
     * Replaces the entire state with [newState].
     */
    protected fun setState(newState: S) {
        _uiState.value = newState
    }
}

