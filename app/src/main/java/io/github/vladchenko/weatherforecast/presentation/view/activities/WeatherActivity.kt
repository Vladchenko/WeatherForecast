package io.github.vladchenko.weatherforecast.presentation.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.presentation.coordinator.NetworkStatusCoordinator
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.util.hideBottomNavigationBar
import io.github.vladchenko.weatherforecast.presentation.util.setLightStatusBars
import io.github.vladchenko.weatherforecast.presentation.util.setTransparentSystemBars
import io.github.vladchenko.weatherforecast.utils.ResourceManager
import javax.inject.Inject

/**
 * Main activity of the Weather Forecast application.
 *
 * Serves as the entry point for the app UI and hosts navigation components.
 * Uses Hilt for dependency injection, including [WorkManager] for background tasks.
 *
 * The layout is defined in [R.layout.weather_forecast_activity] and typically contains
 * a NavHostFragment to manage screen navigation.
 */
@AndroidEntryPoint
class WeatherActivity : AppCompatActivity() {

    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var connectivityObserver: ConnectivityObserver
    @Inject lateinit var statusRenderer: StatusRenderer
    @Inject lateinit var resourceManager: ResourceManager

    private lateinit var networkCoordinator: NetworkStatusCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkCoordinator = NetworkStatusCoordinator(
            connectivityObserver = connectivityObserver,
            statusRenderer = statusRenderer,
            resourceManager = resourceManager
        )

        lifecycle.addObserver(networkCoordinator)
        setContentView(R.layout.weather_forecast_activity)

        initNetworkCoordinator()
    }

    override fun onResume() {
        super.onResume()
        setTransparentSystemBars()
        setLightStatusBars(isLight = isLightTheme())
        hideBottomNavigationBar()
    }

    private fun initNetworkCoordinator() {
        networkCoordinator = NetworkStatusCoordinator(
            connectivityObserver = connectivityObserver,
            statusRenderer = statusRenderer,
            resourceManager = resourceManager
        )
        lifecycle.addObserver(networkCoordinator)
    }

    private fun isLightTheme(): Boolean {
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode != android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}