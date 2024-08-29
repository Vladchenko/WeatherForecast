package com.example.weatherforecast.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.weatherforecast.R
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.models.presentation.MessageType
import com.example.weatherforecast.models.presentation.ToolbarSubtitleMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * View model (MVVM component) with code common to inherent viewModels.
 *
 * @property coroutineDispatchers dispatchers for coroutines
 */
open class AbstractViewModel(
    private val coroutineDispatchers: CoroutineDispatchers
) : ViewModel() {

    internal val showProgressBarState: MutableState<Boolean> = mutableStateOf(true)
    private val _toolbarSubtitleMessageState: MutableStateFlow<ToolbarSubtitleMessage> = MutableStateFlow(
        ToolbarSubtitleMessage(null, null, MessageType.INFO)
    )
    val toolbarSubtitleMessageState: StateFlow<ToolbarSubtitleMessage>
        get() = _toolbarSubtitleMessageState

    /**
     * Show [statusMessage].
     */
    fun showStatus(statusMessage: String) {
        _toolbarSubtitleMessageState.value = ToolbarSubtitleMessage(null, statusMessage, MessageType.INFO)
    }

    /**
     * Show status message, providing [stringResId].
     */
    fun showStatus(@StringRes stringResId: Int) {
        _toolbarSubtitleMessageState.value = ToolbarSubtitleMessage(stringResId, null, MessageType.INFO)
    }

    /**
     * Show status message, providing [stringResId] and [value] as argument.
     */
    fun showStatus(@StringRes stringResId: Int, value: String) {
        _toolbarSubtitleMessageState.value = ToolbarSubtitleMessage(stringResId, value, MessageType.INFO)
    }

    /**
     * Show [errorMessage].
     */
    fun showError(errorMessage: String) {
        _toolbarSubtitleMessageState.value = ToolbarSubtitleMessage(null, errorMessage, MessageType.ERROR)
    }

    /**
     * Show error message, providing [stringResId].
     */
    fun showError(@StringRes stringResId: Int) {
        _toolbarSubtitleMessageState.value = ToolbarSubtitleMessage(stringResId, null, MessageType.ERROR)
    }

    /**
     * Show error message, providing [stringResId] and [value] as argument.
     */
    fun showError(@StringRes stringResId: Int, value: String) {
        _toolbarSubtitleMessageState.value = ToolbarSubtitleMessage(stringResId, value, MessageType.ERROR)
    }

    /**
     * Show message in toolbar subtitle for [chosenCity] for first downloading run.
     */
    fun showInitialDownloadingStatusForCity(chosenCity: String) {
        if (chosenCity.isBlank()) {
            _toolbarSubtitleMessageState.value = ToolbarSubtitleMessage(
                R.string.forecast_downloading,
                null,
                MessageType.INFO
            )
        } else {
            _toolbarSubtitleMessageState.value = ToolbarSubtitleMessage(
                R.string.forecast_downloading_for_city_text,
                chosenCity,
                MessageType.INFO
            )
        }
    }
}