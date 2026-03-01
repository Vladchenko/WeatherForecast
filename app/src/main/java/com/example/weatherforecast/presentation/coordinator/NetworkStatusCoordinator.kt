package com.example.weatherforecast.presentation.coordinator

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.utils.ResourceManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Coordinator that observes network connectivity and shows appropriate status messages.
 *
 * Automatically subscribes to [ConnectivityObserver] and uses [StatusRenderer]
 * to display "Connected"/"Disconnected" messages.
 *
 * @param connectivityObserver Source of network state
 * @param statusRenderer Handles display of status messages
 * @param resourceManager Provides string resources
 */
class NetworkStatusCoordinator(
    private val connectivityObserver: ConnectivityObserver,
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager
) : DefaultLifecycleObserver {

    /**
     * Start observing connectivity when lifecycle reaches STARTED state.
     */
    override fun onStart(owner: LifecycleOwner) {
        connectivityObserver.isConnected
            .onEach { isConnected ->
                if (isConnected) {
                    statusRenderer.showStatus(resourceManager.getString(R.string.network_connected))
                } else {
                    statusRenderer.showError(resourceManager.getString(R.string.network_disconnected))
                }
            }
            .launchIn(owner.lifecycle.coroutineScope)
    }
}