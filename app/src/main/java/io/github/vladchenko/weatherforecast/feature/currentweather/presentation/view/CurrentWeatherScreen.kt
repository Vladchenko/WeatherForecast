package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.navigation.NavigationEventBus
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEventDispatcher
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel

/**
 * The root composable for the main weather forecast screen.
 *
 * This function loads and displays current weather data based on the provided
 * [cityModel] parameter. It handles navigation events from [CurrentWeatherViewModel]
 * and provides lifecycle-safe data flow.
 *
 * ## Supported Features
 * - Automatic initial data loading on composition start
 * - Pull-to-refresh functionality for manual data reload
 * - Hourly forecast panel with lazy loading support
 * - App-level actions (close app, navigate to city search)
 *
 * The UI is rendered using [CurrentWeatherLayout], and navigation is delegated
 * via the [NavigationEventDispatcher].
 *
 * @param cityModel Represents data for city to provide a weather forecast on
 * @param navigationEventBus The event bus for dispatching navigation events
 * @param appBarViewModel The toolbar state provider. Default: Hilt-provided instance.
 * @param hourlyViewModel The hourly forecast state manager. Default: Hilt-provided instance.
 * @param weatherViewModel The current weather state manager. Default: Hilt-provided instance.
 */
@ExperimentalMaterial3Api
@Composable
fun CurrentWeatherScreen(
    cityModel: CityLocationModel,
    navigationEventBus: NavigationEventBus,
    appBarViewModel: AppBarViewModel = hiltViewModel(),
    hourlyViewModel: HourlyWeatherViewModel = hiltViewModel(),
    weatherViewModel: CurrentWeatherViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val weatherUiState by weatherViewModel.weatherStateFlow.collectAsStateWithLifecycle()
    val appBarUiState by appBarViewModel.appBarUiStateFlow.collectAsStateWithLifecycle()
    val hourlyWeatherUiState by hourlyViewModel.hourlyWeatherStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(cityModel) {
        appBarViewModel.updateTitle(context.getString(R.string.app_name))
        weatherViewModel.launchWeatherForecast(cityModel)
    }

    CurrentWeatherLayout(
        appBarUiState = appBarUiState,
        weatherUiState = weatherUiState,
        navigationEventBus = navigationEventBus,
        hourlyWeatherUiState = hourlyWeatherUiState,
        onRefreshWeather = { weatherViewModel.refreshWeather(true) },
        onLoadHourlyWeather = { data -> hourlyViewModel.loadHourlyWeatherForLocation(data) }
    )
}