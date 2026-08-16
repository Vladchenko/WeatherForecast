package io.github.vladchenko.weatherforecast.core.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing the application's network connectivity state.
 *
 * This interface acts as a lightweight state holder that exposes the current network
 * connectivity status via a [StateFlow]. It decouples the underlying connectivity observation
 * mechanism from the UI and domain layers, providing a clean reactive API.
 *
 * The state is updated externally via [updateState], typically by presentation-layer components
 * (e.g., [NetworkStatusCoordinator]) that monitor system-level connectivity events.
 *
 * @see ConnectivityObserver
 * @see NetworkStatusCoordinator
 */
interface NetworkStateHolder {

    /**
     * Represents the current network connectivity state.
     *
     * - `true`: Network is available and connected.
     * - `false`: Network is unavailable or disconnected.
     */
    val networkStateFlow: StateFlow<Boolean>

    /**
     * Updates the current network connectivity state.
     *
     * This method is called by external components (typically [NetworkStatusCoordinator])
     * to broadcast state changes to all active collectors of [networkStateFlow].
     *
     * @param state the new connectivity state (`true` if connected, `false` otherwise).
     */
    fun updateState(state: Boolean)
}