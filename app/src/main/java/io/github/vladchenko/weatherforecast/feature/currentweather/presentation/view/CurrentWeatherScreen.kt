package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEventDispatcher
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel

/**
 * The root composable for the main weather forecast screen.
 *
 * This function loads and displays current weather data based on the provided
 * [initialCity], [initialLat], and [initialLon] parameters. It handles navigation
 * events from [CurrentWeatherViewModel] and provides lifecycle-safe data flow.
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
 * @param initialLat The starting latitude for weather data retrieval.
 * @param initialLon The starting longitude for weather data retrieval.
 * @param initialCity The starting city name for weather data display.
 * @param navigationEventDispatcher Dispatcher for handling navigation events.
 * @param appBarViewModel The toolbar state provider. Default: Hilt-provided instance.
 * @param hourlyViewModel The hourly forecast state manager. Default: Hilt-provided instance.
 * @param weatherViewModel The current weather state manager. Default: Hilt-provided instance.
 */
@ExperimentalMaterial3Api
@Composable
fun CurrentWeatherScreen(
    initialLat: Double,
    initialLon: Double,
    initialCity: String,
    appBarViewModel: AppBarViewModel = hiltViewModel(),
    navigationEventDispatcher: NavigationEventDispatcher,
    hourlyViewModel: HourlyWeatherViewModel = hiltViewModel(),
    weatherViewModel: CurrentWeatherViewModel = hiltViewModel(),
) {
    val weatherUiState by weatherViewModel.weatherStateFlow.collectAsStateWithLifecycle()
    val refreshingState by weatherViewModel.refreshingStateFlow.collectAsStateWithLifecycle()
    val appBarUiState by appBarViewModel.appBarUiStateFlow.collectAsStateWithLifecycle()
    val hourlyWeatherUiState by hourlyViewModel.hourlyWeatherStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(initialCity, initialLat, initialLon) {
        weatherViewModel.launchWeatherForecast(initialCity, initialLat, initialLon)
    }

    CurrentWeatherLayout(
        appBarUiState = appBarUiState,
        weatherUiState = weatherUiState,
        refreshingState = refreshingState,
        hourlyWeatherUiState = hourlyWeatherUiState,
        navigationEventDispatcher = navigationEventDispatcher,
        onRefreshWeather = { weatherViewModel.refreshWeather(true) },
        onLoadHourlyWeather = { data -> hourlyViewModel.loadHourlyWeatherForLocation(data) }
    )
}