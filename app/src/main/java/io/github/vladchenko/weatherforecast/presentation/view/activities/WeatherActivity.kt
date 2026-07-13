package io.github.vladchenko.weatherforecast.presentation.view.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.systembars.hideBottomNavigationBar
import io.github.vladchenko.weatherforecast.core.ui.systembars.setLightStatusBars
import io.github.vladchenko.weatherforecast.core.ui.systembars.setTransparentSystemBars
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.coordinator.NetworkStatusCoordinator
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEventDispatcherImpl
import io.github.vladchenko.weatherforecast.presentation.navigation.WeatherAppNavHost
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.theme.WeatherForecastTheme
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

/**
 * Main activity serving as the app's entry point.
 * Hosts navigation graph and manages system UI appearance.
 *
 * Key features:
 * Manages [WeatherAppNavHost] for screen navigation
 * Coordinates network connectivity via [NetworkStatusCoordinator]
 * Provides shared view models for weather, forecast, and city search
 * Configures status and navigation bars appearance
 */
@FlowPreview
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

    private val appBarViewModel: AppBarViewModel by viewModels()
    private val citySearchViewModel: CitySearchViewModel by viewModels()
    private val weatherViewModel: CurrentWeatherViewModel by viewModels()

    private val hourlyWeatherViewModel: HourlyWeatherViewModel by viewModels()

    private lateinit var networkCoordinator: NetworkStatusCoordinator

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val navigationDispatcher = NavigationEventDispatcherImpl(
                navController,
                {
                    this.finishAffinity()
                } )
            initNetworkCoordinator(navController)
            WeatherForecastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherAppNavHost(
                        navController = navController,
                        appBarViewModel = appBarViewModel,
                        weatherViewModel = weatherViewModel,
                        hourlyViewModel = hourlyWeatherViewModel,
                        citySearchViewModel = citySearchViewModel,
                        navigationDispatcher = navigationDispatcher
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setTransparentSystemBars()
        setLightStatusBars(isLight = isLightTheme())
        hideBottomNavigationBar()
    }

    private fun initNetworkCoordinator(navController: NavController) {
        if (::networkCoordinator.isInitialized) return
        networkCoordinator = NetworkStatusCoordinator(
            navController = navController,
            statusRenderer = statusRenderer,
            resourceManager = resourceManager,
            connectivityObserver = connectivityObserver,
            currentWeatherViewModel = weatherViewModel
        )
        lifecycle.addObserver(networkCoordinator)
    }

    private fun isLightTheme(): Boolean {
        val currentNightMode =
            resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode != android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}