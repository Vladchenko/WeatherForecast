package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.navigation.api.NavigationEventBus
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.navigation.WeatherAppNavHost
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel

/**
 * Root composable for the main weather forecast screen.
 *
 * This function loads and displays current weather data based on the [cityModel] parameter
 * passed from the navigation graph. When [cityModel] changes
 * (e.g., user selects a different city from search) [LaunchedEffect] triggers
 * [CurrentWeatherViewModel.launchWeatherForecast] to fetch and display weather
 * for the new location.
 *
 * ## Navigation
 * All navigation actions are delegated through callback functions provided by
 * [WeatherAppNavHost], which route them to [NavigationEventBus]. This keeps
 * the screen decoupled from navigation implementation details.
 *
 * ## Data Flow
 * 1. [CurrentWeatherViewModel] manages weather state via [CurrentWeatherViewModel.weatherStateFlow]
 * 2. On composition [LaunchedEffect(cityModel)] triggers initial weather loading
 * 3. User actions (refresh, navigate to city/settings) are passed as callbacks to [CurrentWeatherLayout]
 *
 * ## Supported Features
 * - Automatic weather loading on city change via [LaunchedEffect]
 * - Pull-to-refresh functionality for manual data reload
 * - Hourly forecast panel with lazy loading support
 * - Navigation to city search and settings screens
 *
 * @param onCloseApp callback to close the app ("Back" button)
 * @param cityModel city data for which to display weather.
 *                  Changing this parameter triggers a new weather load
 * @param onNavigateToSettings callback to navigate to settings screen
 * @param onNavigateToAiSummary callback to navigate to AI analysis screen
 * @param onNavigateToCitySelection callback to navigate to city selection screen
 * @param appBarViewModel toolbar state provider. Default: instance from Hilt
 * @param hourlyViewModel hourly forecast state manager. Default: instance from Hilt
 * @param weatherViewModel current weather state manager. Default: instance from Hilt
 */
@ExperimentalMaterial3Api
@Composable
fun CurrentWeatherScreen(
    onCloseApp: () -> Unit,
    cityModel: CityLocationModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToAiSummary: () -> Unit,
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
        onNavigateToAiSummary = onNavigateToAiSummary,
        onNavigateToCitySelection = onNavigateToCitySelection,
        onRefreshWeather = { weatherViewModel.refreshWeather(true) },
        onLoadHourlyWeather = { data -> hourlyViewModel.loadHourlyWeatherForLocation(data) }
    )
}