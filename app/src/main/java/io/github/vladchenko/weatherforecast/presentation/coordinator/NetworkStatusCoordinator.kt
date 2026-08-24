package io.github.vladchenko.weatherforecast.presentation.coordinator

import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

/**
 * A lifecycle-aware coordinator that monitors network connectivity and reacts to state changes.
 *
 * This class observes [ConnectivityObserver.isConnected] and performs two main actions:
 * 1. Updates the global UI status via [StatusStateHolder] ("Connected" / "Disconnected").
 * 2. Automatically refreshes weather data upon reconnection, but **only** when the user is on the weather forecast screen.
 *
 * @property applicationScope The coroutine scope bound to the application lifecycle.
 * @property statusStateHolder Manages and broadcasts UI status messages (info for connected, error for disconnected).
 * @property networkStateHolder Manages network connectivity (connect or disconnect)
 * @property connectivityObserver Provides the stream of network connectivity states.
 */
class NetworkStatusCoordinator(
    private val applicationScope: CoroutineScope,
    private val statusStateHolder: StatusStateHolder,
    private val networkStateHolder: NetworkStateHolder,
    private val connectivityObserver: ConnectivityObserver,
) {

    /**
     * Tracks the previous network connection state to prevent duplicate UI updates
     * and redundant weather refreshes.
     */
    private var lastConnectionState: Boolean? = null

    init {
        startObserving(applicationScope)
    }

    /**
     * Starts observing network connectivity within the provided scope.
     *
     * Subscribes to [connectivityObserver.isConnected] with the following behavior:
     * - Filters out duplicate emissions using [distinctUntilChanged].
     * - Shares the flow in the provided scope with a 5-second replay timeout ([WhileSubscribed]).
     * - Updates UI status and network state via [StatusStateHolder] and [NetworkStateHolder] upon connection changes.
     *
     * @param scope The [CoroutineScope] in which to run the observation.
     */
    fun startObserving(scope: CoroutineScope) {
        connectivityObserver.isConnected
            .distinctUntilChanged()
            .shareIn(
                scope,
                WhileSubscribed(5000),
                0
            )
            .onEach { isConnected ->
                if (lastConnectionState != isConnected) {
                    when (isConnected) {
                        true -> {
                            statusStateHolder.updateInfoStatus(R.string.network_connected)
                            networkStateHolder.updateState(true)
                        }

                        false -> {
                            statusStateHolder.updateErrorStatus(R.string.network_disconnected)
                            networkStateHolder.updateState(false)
                        }
                    }
                    lastConnectionState = isConnected
                }
            }
            .launchIn(scope)
    }
}