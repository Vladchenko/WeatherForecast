package io.github.vladchenko.weatherforecast.core.network

import io.github.vladchenko.weatherforecast.presentation.coordinator.NetworkStatusCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of [NetworkStateHolder] that manages network connectivity state.
 *
 * This class maintains the current network connectivity status using a [MutableStateFlow]
 * and exposes it as an immutable [StateFlow] for observation by other components.
 *
 * The initial state is set to `true` (connected) by default. External components,
 * such as [NetworkStatusCoordinator], call [updateState] to notify this holder
 * about connectivity changes observed from system-level events.
 */
class NetworkStateHolderImpl : NetworkStateHolder {

    override val networkStateFlow: StateFlow<Boolean>
        get() = _networkStateHolder

    private val _networkStateHolder = MutableStateFlow(true)

    override fun updateState(state: Boolean) {
        _networkStateHolder.value = state
    }
}