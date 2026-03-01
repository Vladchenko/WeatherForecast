package com.example.weatherforecast.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.connectivity.ConnectivityObserver
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * View model (MVVM component) with code common to inherent viewModels.
 *
 * @param connectivityObserver internet connectivity observer
 */
open class AbstractViewModel(
    protected open val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    /**
     * A mutable state that controls whether a progress bar should be shown in the UI.
     *
     * This state is typically observed in composables via `collectAsState()`.
     * Update this value directly (`showProgressBarState.value = true`) in subclasses
     * when a loading state needs to be reflected.
     */
    internal val showProgressBarState: MutableState<Boolean> = mutableStateOf(true)

    /**
     * A [StateFlow] that emits the current internet connectivity status.
     *
     * The value is `true` if the device has an active network connection,
     * and `false` otherwise. This state is derived from [connectivityObserver]
     * and shared via [SharingStarted.WhileSubscribed], meaning it stops collecting
     * when no observers are present (for more than 5 seconds), saving resources.
     *
     * It also emits an initial value of `true` while waiting for the first real check.
     */
    val internetConnectedStateFlow: StateFlow<Boolean> = connectivityObserver
        .isConnected
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            true
        )
}