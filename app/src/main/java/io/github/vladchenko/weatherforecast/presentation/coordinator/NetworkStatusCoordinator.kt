package io.github.vladchenko.weatherforecast.presentation.coordinator

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavController
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.CITY_SEARCH
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.WEATHER
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
 * It prevents unnecessary updates by checking the current navigation route via [navController]
 * and tracks the last known connection state to avoid redundant processing.
 *
 * @property navController Navigation controller used to determine the current screen and decide whether to refresh weather data.
 * @property resourceManager Provides localized string resources for UI messages.
 * @property statusStateHolder Manages and broadcasts UI status messages (info for connected, error for disconnected).
 * @property networkStateHolder Manages network connectivity (connect or disconnect)
 * @property connectivityObserver Provides the stream of network connectivity states.
 */
class NetworkStatusCoordinator(
    private val navController: NavController,
    private val resourceManager: ResourceManager,
    private val statusStateHolder: StatusStateHolder,
    private val networkStateHolder: NetworkStateHolder,
    private val connectivityObserver: ConnectivityObserver,
) : DefaultLifecycleObserver {

    /**
     * Tracks the previous network connection state to prevent duplicate UI updates
     * and redundant weather refreshes.
     */
    private var lastConnectionState: Boolean? = null

    /**
     * Starts observing network connectivity when the lifecycle owner enters the [Lifecycle.State.STARTED] state.
     *
     * Subscribes to [connectivityObserver.isConnected] with the following behavior:
     * - Filters out duplicate emissions using [distinctUntilChanged].
     * - Shares the flow in the lifecycle's coroutine scope with a 5-second replay timeout ([WhileSubscribed]).
     * - When connection is restored (`true`):
     *   - Checks the current navigation route via [navController].
     *   - If on the weather screen, triggers a weather refresh via [CurrentWeatherViewModel.refreshWeather].
     *   - Displays a "Connected" status message.
     * - When connection is lost (`false`):
     *   - Displays a "Disconnected" error message.
     *
     * Ensures resource-efficient observation that automatically respects the lifecycle owner's state.
     *
     * @param owner The lifecycle owner (e.g., Activity) controlling the observation lifetime.
     */
    override fun onStart(owner: LifecycleOwner) {
        connectivityObserver.isConnected
            .distinctUntilChanged()
            .shareIn(
                owner.lifecycle.coroutineScope,
                WhileSubscribed(5000),
                0
            )
            .onEach { isConnected ->
                if (lastConnectionState != isConnected) {
                    when (isConnected) {
                        true -> {
                            val route = navController.currentDestination?.route.orEmpty()
                            when {
                                route.contains(WEATHER) -> {
//                                    currentWeatherViewModel.refreshWeather(false)
                                    networkStateHolder.updateState(true)
                                }

                                route == CITY_SEARCH -> {
                                    // Do nothing
                                }
                            }
                            statusStateHolder.updateStatus(
                                StatusType.Info(
                                    resourceManager.getString(
                                        R.string.network_connected
                                    )
                                )
                            )
                        }

                        false -> {
                            statusStateHolder.updateStatus(
                                StatusType.Error(resourceManager.getString(R.string.network_disconnected))
                            )
                            networkStateHolder.updateState(false)
                        }
                    }
                    lastConnectionState = isConnected
                }
            }
            .launchIn(owner.lifecycle.coroutineScope)
    }
}