package com.dimas.dimasproject.feature.randomnumber.presentation

import androidx.lifecycle.viewModelScope
import com.dimas.dimasproject.core.presentation.BaseViewModel
import com.dimas.dimasproject.feature.randomnumber.domain.usecase.FetchRandomNumberUseCase
import com.dimas.dimasproject.feature.randomnumber.domain.usecase.GetAllRandomNumbersUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RandomNumberViewModel(
    private val fetchRandomNumber: FetchRandomNumberUseCase,
    private val getAllRandomNumbers: GetAllRandomNumbersUseCase
) : BaseViewModel<RandomNumberUiState, RandomNumberUiEvent>(RandomNumberUiState()) {

    init {
        observeHistory()
    }

    override fun handleIntent(intent: RandomNumberUiEvent) {
        when (intent) {
            is RandomNumberUiEvent.FetchRandom -> fetchRandom()
            is RandomNumberUiEvent.DismissError -> dismissError()
        }
    }

    private fun observeHistory() {
        getAllRandomNumbers()
            .onEach { list ->
                updateState { copy(history = list, latest = list.firstOrNull()) }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchRandom() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, errorMessage = null) }
            fetchRandomNumber()
                .onSuccess { result ->
                    updateState { copy(isLoading = false, latest = result) }
                }
                .onFailure { error ->
                    updateState { copy(isLoading = false, errorMessage = error.message ?: "Unknown error") }
                }
        }
    }

    private fun dismissError() {
        updateState { copy(errorMessage = null) }
    }
}
