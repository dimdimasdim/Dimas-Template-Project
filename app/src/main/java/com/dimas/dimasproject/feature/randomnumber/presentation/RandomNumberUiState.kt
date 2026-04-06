package com.dimas.dimasproject.feature.randomnumber.presentation

import com.dimas.dimasproject.core.presentation.BaseIntent
import com.dimas.dimasproject.core.presentation.BaseUiState
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber

data class RandomNumberUiState(
    val isLoading: Boolean = false,
    val latest: RandomNumber? = null,
    val history: List<RandomNumber> = emptyList(),
    val errorMessage: String? = null
) : BaseUiState

sealed interface RandomNumberUiEvent : BaseIntent {
    data object FetchRandom : RandomNumberUiEvent
    data object DismissError : RandomNumberUiEvent
}
