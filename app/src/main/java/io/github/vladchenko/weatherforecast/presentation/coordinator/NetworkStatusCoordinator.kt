package io.github.vladchenko.weatherforecast.presentation.coordinator

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.model.CurrentScreen
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.navigation.WeatherNavigator
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

/**
 * A lifecycle-aware coordinator that monitors network connectivity and reacts accordingly.
 *
 * Observes [ConnectivityObserver.isConnected] and:
 * - Displays user-friendly status messages via [StatusRenderer] ("Connected" / "Disconnected")
 * - Automatically refreshes weather data on reconnection, but **only** when the user is on the weather screen
 * - Prevents unnecessary updates on the city selection screen
 * - Ensures resource-efficient observation using [WhileSubscribed(5000)]
 * - Avoids duplicate UI updates with [distinctUntilChanged]
 *
 * The coordinator integrates with the app's navigation state via [WeatherNavigator] to make
 * context-aware decisions (e.g., not triggering weather refresh on city selection screen).
 *
 * @property statusRenderer Renders status and error messages to the UI
 * @property resourceManager Provides localized string resources
 * @property weatherNavigator Determines the current screen to enable context-sensitive behavior
 * @property connectivityObserver Source of network connectivity state
 * @property currentWeatherViewModel Triggers weather data refresh when connection is restored
 */
class NetworkStatusCoordinator(
    private val statusRenderer: StatusRenderer,
    private val resourceManager: ResourceManager,
    private val weatherNavigator: WeatherNavigator,
    private val connectivityObserver: ConnectivityObserver,
    private val currentWeatherViewModel: CurrentWeatherViewModel
) : DefaultLifecycleObserver {

    private var lastConnectionState: Boolean? = null

    /**
     * Starts observing network connectivity when the lifecycle owner enters the STARTED state.
     *
     * Subscribes to [connectivityObserver.isConnected] and:
     * - Displays a "Connected" status message via [statusRenderer] when connection is established.
     * - Triggers weather refresh in [currentWeatherViewModel] only if the current screen is [CurrentScreen.Weather].
     * - Displays a "Disconnected" error message when connection is lost.
     *
     * Uses [WhileSubscribed] with a 5-second timeout to balance responsiveness and resource efficiency.
     * Duplicate state emissions are suppressed using [distinctUntilChanged].
     *
     * @param owner The lifecycle owner (e.g., Activity or Fragment) that controls observation lifetime.
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
                            when(weatherNavigator.getCurrentDestination()) {
                                CurrentScreen.Weather -> {
                                    currentWeatherViewModel.refreshWeather()
                                }
                                CurrentScreen.CitySelection -> {}
                            }
                            statusRenderer.showStatus(resourceManager.getString(R.string.network_connected))
                        }
                        false -> statusRenderer.showError(resourceManager.getString(R.string.network_disconnected))
                    }
                    lastConnectionState = isConnected
                }
            }
            .launchIn(owner.lifecycle.coroutineScope)
    }
}