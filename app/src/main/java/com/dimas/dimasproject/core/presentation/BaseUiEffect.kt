package com.dimas.dimasproject.core.presentation

/**
 * Marker interface for all one-time UI side effects (e.g. navigation, toast, snackbar).
 *
 * Unlike [BaseUiState] which is persistent and survives recomposition,
 * effects are delivered exactly once through a [kotlinx.coroutines.channels.Channel]
 * and collected as a SharedFlow in the View layer.
 *
 * Examples: ShowSnackbar, NavigateTo, ShowToast, OpenDialog
 */
interface BaseUiEffect

