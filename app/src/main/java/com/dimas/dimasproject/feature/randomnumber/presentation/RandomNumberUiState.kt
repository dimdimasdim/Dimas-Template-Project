package com.dimas.dimasproject.feature.randomnumber.presentation

import com.dimas.dimasproject.core.presentation.BaseIntent
import com.dimas.dimasproject.core.presentation.BaseUiEffect
import com.dimas.dimasproject.core.presentation.BaseUiState
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber

// ── State (Model) ────────────────────────────────────────────────────────────────
data class RandomNumberUiState(
    val isLoading: Boolean = false,
    val latest: RandomNumber? = null,
    val history: List<RandomNumber> = emptyList()
) : BaseUiState

// ── Intent ───────────────────────────────────────────────────────────────────────
sealed interface RandomNumberUiEvent : BaseIntent {
    data object FetchRandom : RandomNumberUiEvent
}

// ── Effect (one-time) ────────────────────────────────────────────────────────────
sealed interface RandomNumberUiEffect : BaseUiEffect {
    /** Show a snackbar message once — success or error. */
    data class ShowSnackbar(val message: String) : RandomNumberUiEffect
}
