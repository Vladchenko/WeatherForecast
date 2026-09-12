package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.navigation.NavigationEventBus
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel

/**
 * The root composable for the main weather forecast screen.
 *
 * This function loads and displays current weather data based on the [cityModel]
 * parameter passed from the navigation graph. When the [cityModel] changes
 * (e.g., user selects a different city from search), [LaunchedEffect] triggers
 * [CurrentWeatherViewModel.launchWeatherForecast] to fetch and display weather
 * for the new location.
 *
 * ## Navigation
 * All navigation actions are delegated via callback functions provided by
 * [WeatherAppNavHost], which forwards them to [NavigationEventBus]. This keeps
 * the screen decoupled from navigation implementation details.
 *
 * ## Data Flow
 * 1. [CurrentWeatherViewModel] manages weather state via [CurrentWeatherViewModel.weatherStateFlow]
 * 2. On composition, [LaunchedEffect(cityModel)] triggers initial weather load
 * 3. User actions (refresh, navigate to city/settings) are passed as callbacks to [CurrentWeatherLayout]
 *
 * ## Supported Features
 * - Automatic weather loading on city change via [LaunchedEffect]
 * - Pull-to-refresh functionality for manual data reload
 * - Hourly forecast panel with lazy loading support
 * - Navigation to city search and settings screens
 *
 * @param onCloseApp Callback for closing the app (back button).
 * @param cityModel Represents data for the city to display weather for.
 *                  Changes to this parameter trigger a new weather fetch.
 * @param onNavigateToSettings Callback for navigating to settings screen.
 * @param onNavigateToCitySelection Callback for navigating to city selection screen.
 * @param appBarViewModel The toolbar state provider. Default: Hilt-provided instance.
 * @param hourlyViewModel The hourly forecast state manager. Default: Hilt-provided instance.
 * @param weatherViewModel The current weather state manager. Default: Hilt-provided instance.
 */
@ExperimentalMaterial3Api
@Composable
fun CurrentWeatherScreen(
    onCloseApp: () -> Unit,
    cityModel: CityLocationModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToCitySelection: () -> Unit,
    appBarViewModel: AppBarViewModel = hiltViewModel(),
    hourlyViewModel: HourlyWeatherViewModel = hiltViewModel(),
    weatherViewModel: CurrentWeatherViewModel = hiltViewModel(),
) {
    val weatherUiState by weatherViewModel.weatherStateFlow.collectAsStateWithLifecycle()
    val appBarUiState by appBarViewModel.appBarUiStateFlow.collectAsStateWithLifecycle()
    val hourlyWeatherUiState by hourlyViewModel.hourlyWeatherStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(cityModel) {
        weatherViewModel.launchWeatherForecast(cityModel)
    }

    CurrentWeatherLayout(
        onCloseApp = onCloseApp,
        appBarUiState = appBarUiState,
        weatherUiState = weatherUiState,
        onNavigateToSettings = onNavigateToSettings,
        hourlyWeatherUiState = hourlyWeatherUiState,
        onNavigateToCitySelection = onNavigateToCitySelection,
        onRefreshWeather = { weatherViewModel.refreshWeather(true) },
        onLoadHourlyWeather = { data -> hourlyViewModel.loadHourlyWeatherForLocation(data) }
    )
}