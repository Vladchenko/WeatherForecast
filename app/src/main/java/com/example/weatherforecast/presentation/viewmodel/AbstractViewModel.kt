package com.example.weatherforecast.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.models.presentation.MessageType
import com.example.weatherforecast.models.presentation.ToolbarSubtitleMessage
import com.example.weatherforecast.presentation.PresentationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * View model (MVVM component) with code common to inherent viewModels.
 *
 * @param connectivityObserver internet connectivity observer
 * @property coroutineDispatchers dispatchers for coroutines
 */
open class AbstractViewModel(
    connectivityObserver: ConnectivityObserver,
    private val coroutineDispatchers: CoroutineDispatchers
) : ViewModel() {

    internal val showProgressBarState: MutableState<Boolean> = mutableStateOf(true)

    val toolbarSubtitleMessageFlow: StateFlow<ToolbarSubtitleMessage>
        get() = _toolbarSubtitleMessageFlow
    val internetConnectedState: StateFlow<Boolean> = connectivityObserver
        .isConnected
        .stateIn(     // Convert the Flow to a StateFlow (cold flow to a hot flow)
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            true
        )

    private val _toolbarSubtitleMessageFlow: MutableStateFlow<ToolbarSubtitleMessage> =
        MutableStateFlow(
            ToolbarSubtitleMessage(
                null,
                null,
                PresentationUtils.getToolbarSubtitleColor(MessageType.INFO),
                MessageType.INFO
            )
        )

    init {
        collectConnectivityEvents()
    }

    private fun collectConnectivityEvents() {
        viewModelScope.launch(coroutineDispatchers.io) {
            internetConnectedState.collect { isConnected ->
                if (isConnected) {
                    showStatus(R.string.connected)
                } else {
                    showError(R.string.disconnected)
                }
            }
        }
    }

    /**
     * Show [statusMessage].
     */
    fun showStatus(statusMessage: String) {
        _toolbarSubtitleMessageFlow.value =
            ToolbarSubtitleMessage(
                null,
                statusMessage,
                PresentationUtils.getToolbarSubtitleColor(MessageType.INFO),
                MessageType.INFO
            )
    }

    /**
     * Show status message, providing [stringResId].
     */
    fun showStatus(@StringRes stringResId: Int) {
        _toolbarSubtitleMessageFlow.value =
            ToolbarSubtitleMessage(
                stringResId,
                null,
                PresentationUtils.getToolbarSubtitleColor(MessageType.INFO),
                MessageType.INFO
            )
    }

    /**
     * Show status message, providing [stringResId] and [value] as argument.
     */
    fun showStatus(@StringRes stringResId: Int, value: String) {
        _toolbarSubtitleMessageFlow.value =
            ToolbarSubtitleMessage(
                stringResId, value,
                PresentationUtils.getToolbarSubtitleColor(MessageType.INFO), MessageType.INFO
            )
    }

    /**
     * Show [warningMessage].
     */
    fun showWarning(warningMessage: String) {
        _toolbarSubtitleMessageFlow.value =
            ToolbarSubtitleMessage(
                null,
                warningMessage,
                PresentationUtils.getToolbarSubtitleColor(MessageType.WARNING),
                MessageType.WARNING
            )
    }

    /**
     * Show warning message, providing [stringResId] and [value] as argument.
     */
    fun showWarning(@StringRes stringResId: Int, value: String) {
        _toolbarSubtitleMessageFlow.value =
            ToolbarSubtitleMessage(
                stringResId,
                value,
                PresentationUtils.getToolbarSubtitleColor(MessageType.WARNING),
                MessageType.WARNING
            )
    }

    /**
     * Show [errorMessage].
     */
    fun showError(errorMessage: String) {
        _toolbarSubtitleMessageFlow.value =
            ToolbarSubtitleMessage(
                null,
                errorMessage,
                PresentationUtils.getToolbarSubtitleColor(MessageType.ERROR),
                MessageType.ERROR
            )
    }

    /**
     * Show [exception].
     */
    fun showError(exception: Exception) {
        _toolbarSubtitleMessageFlow.value =
            ToolbarSubtitleMessage(
                null,
                exception.message,
                PresentationUtils.getToolbarSubtitleColor(MessageType.ERROR),
                MessageType.ERROR
            )
    }

    /**
     * Show error message, providing [stringResId].
     */
    fun showError(@StringRes stringResId: Int) {
        _toolbarSubtitleMessageFlow.value =
            ToolbarSubtitleMessage(
                stringResId,
                null,
                PresentationUtils.getToolbarSubtitleColor(MessageType.ERROR),
                MessageType.ERROR
            )
    }

    /**
     * Show error message, providing [stringResId] and [value] as argument.
     */
    fun showError(@StringRes stringResId: Int, value: String) {
        _toolbarSubtitleMessageFlow.value =
            ToolbarSubtitleMessage(
                stringResId,
                value,
                PresentationUtils.getToolbarSubtitleColor(MessageType.ERROR),
                MessageType.ERROR
            )
    }

    /**
     * Show message in toolbar subtitle for [chosenCity] for first downloading run.
     */
    fun showInitialDownloadingStatusForCity(chosenCity: String) {
        if (chosenCity.isBlank()) {
            _toolbarSubtitleMessageFlow.value = ToolbarSubtitleMessage(
                R.string.forecast_downloading,
                null,
                PresentationUtils.getToolbarSubtitleColor(MessageType.INFO),
                MessageType.INFO
            )
        } else {
            _toolbarSubtitleMessageFlow.value = ToolbarSubtitleMessage(
                R.string.forecast_downloading_for_city_text,
                chosenCity,
                PresentationUtils.getToolbarSubtitleColor(MessageType.INFO),
                MessageType.INFO
            )
        }
    }
}