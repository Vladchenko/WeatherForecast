package io.github.vladchenko.weatherforecast.presentation.view.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.navigation.WeatherNavigator
import io.github.vladchenko.weatherforecast.core.ui.systembars.hideBottomNavigationBar
import io.github.vladchenko.weatherforecast.core.ui.systembars.setLightStatusBars
import io.github.vladchenko.weatherforecast.core.ui.systembars.setTransparentSystemBars
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.coordinator.NetworkStatusCoordinator
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
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

    @Inject
    lateinit var workManager: WorkManager
    @Inject
    lateinit var connectivityObserver: ConnectivityObserver
    @Inject
    lateinit var statusRenderer: StatusRenderer
    @Inject
    lateinit var resourceManager: ResourceManager

    private val currentWeatherViewModel: CurrentWeatherViewModel by viewModels()

    private lateinit var networkCoordinator: NetworkStatusCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_forecast_activity)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? NavHostFragment
            ?: throw IllegalStateException("NavHostFragment not found")

        val navController = navHostFragment.navController
        val weatherNavigator = WeatherNavigator(navController)

        initNetworkCoordinator(weatherNavigator)
    }

    override fun onResume() {
        super.onResume()
        setTransparentSystemBars()
        setLightStatusBars(isLight = isLightTheme())
        hideBottomNavigationBar()
    }

    private fun initNetworkCoordinator(weatherNavigator: WeatherNavigator) {
        if (::networkCoordinator.isInitialized) return
        networkCoordinator = NetworkStatusCoordinator(
            weatherNavigator = weatherNavigator,
            statusRenderer = statusRenderer,
            resourceManager = resourceManager,
            connectivityObserver = connectivityObserver,
            currentWeatherViewModel = currentWeatherViewModel
        )
        lifecycle.addObserver(networkCoordinator)
    }

    private fun isLightTheme(): Boolean {
        val currentNightMode =
            resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode != android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}