package com.dimas.dimasproject.feature.randomnumber.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimas.dimasproject.feature.randomnumber.domain.usecase.FetchRandomNumberUseCase
import com.dimas.dimasproject.feature.randomnumber.domain.usecase.GetAllRandomNumbersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RandomNumberViewModel(
    private val fetchRandomNumber: FetchRandomNumberUseCase,
    private val getAllRandomNumbers: GetAllRandomNumbersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RandomNumberUiState())
    val uiState: StateFlow<RandomNumberUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    fun onEvent(event: RandomNumberUiEvent) {
        when (event) {
            is RandomNumberUiEvent.FetchRandom -> fetchRandom()
            is RandomNumberUiEvent.DismissError -> dismissError()
        }
    }

    private fun observeHistory() {
        getAllRandomNumbers()
            .onEach { list ->
                _uiState.update { it.copy(history = list, latest = list.firstOrNull()) }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchRandom() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            fetchRandomNumber()
                .onSuccess { result ->
                    _uiState.update { it.copy(isLoading = false, latest = result) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Unknown error")
                    }
                }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

