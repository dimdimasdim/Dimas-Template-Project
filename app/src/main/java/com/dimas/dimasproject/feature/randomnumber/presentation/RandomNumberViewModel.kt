package com.dimas.dimasproject.feature.randomnumber.presentation

import android.util.Log
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
) : BaseViewModel<RandomNumberUiState, RandomNumberUiEvent, RandomNumberUiEffect>(RandomNumberUiState()) {

    init {
        observeHistory()
    }

    override fun handleIntent(intent: RandomNumberUiEvent) {
        when (intent) {
            is RandomNumberUiEvent.FetchRandom -> fetchRandom()
        }
    }

    private fun observeHistory() {
        getAllRandomNumbers()
            .onEach { list ->
                Log.e("RandomNumberViewModel", "Fetched history - Observer : ${list.firstOrNull()} items")
                updateState { copy(history = list, latest = list.firstOrNull()) }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchRandom() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            fetchRandomNumber()
                .onSuccess { result ->
                    updateState { copy(isLoading = false) }
                    Log.e("RandomNumberViewModel", "Fetched history - fetching : $result items")
                    sendEffect(RandomNumberUiEffect.ShowSnackbar("Fetched number: ${result.number} 🎲"))
                }
                .onFailure { error ->
                    updateState { copy(isLoading = false) }
                    sendEffect(RandomNumberUiEffect.ShowSnackbar(error.message ?: "Unknown error"))
                }
        }
    }
}
