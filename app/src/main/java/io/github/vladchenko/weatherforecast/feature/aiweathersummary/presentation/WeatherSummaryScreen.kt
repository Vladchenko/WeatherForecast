package io.github.vladchenko.weatherforecast.feature.aiweathersummary.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vladchenko.weatherforecast.core.presentation.AppBarViewModel

/**
 * Compose screen for displaying the AI weather forecast summary.
 *
 * Retrieves state data through ViewModel and passes it for display
 * in WeatherSummaryLayout. Automatically loads the summary when the screen is first shown.
 *
 * @param onShare callback to share the summary.
 * @param onNavigateUp callback to navigate back.
 * @param appBarViewModel ViewModel for managing top bar state.
 * @param weatherSummaryViewModel ViewModel for managing AI summary state.
 */
@Composable
fun WeatherSummaryScreen(
    onShare: () -> Unit,
    onNavigateUp: () -> Unit,
    appBarViewModel: AppBarViewModel = hiltViewModel(),
    weatherSummaryViewModel: WeatherSummaryViewModel = hiltViewModel()
) {
    val appBarUiState by appBarViewModel.appBarUiStateFlow.collectAsStateWithLifecycle()
    val weatherSummaryUiState by weatherSummaryViewModel.weatherSummary.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        weatherSummaryViewModel.fetchWeatherSummary()
    }

    WeatherSummaryLayout(
        onShare = onShare,
        onNavigateUp = onNavigateUp,
        appBarUiState = appBarUiState,
        weatherSummaryUiState = weatherSummaryUiState,
        onRefresh = { weatherSummaryViewModel.fetchWeatherSummary() }
    )
}