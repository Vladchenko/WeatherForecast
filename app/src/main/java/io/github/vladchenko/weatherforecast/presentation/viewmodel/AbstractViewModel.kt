package io.github.vladchenko.weatherforecast.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

/**
 * View model (MVVM component) with code common to inherent viewModels.
 *
 * @param connectivityObserver internet connectivity observer
 */
open class AbstractViewModel() : ViewModel() {

    /**
     * A mutable state that controls whether a progress bar should be shown in the UI.
     *
     * This state is typically observed in composables via `collectAsState()`.
     * Update this value directly (`showProgressBarState.value = true`) in subclasses
     * when a loading state needs to be reflected.
     */
    internal val showProgressBarState: MutableState<Boolean> = mutableStateOf(true)
}