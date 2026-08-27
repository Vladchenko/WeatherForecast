package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper

import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi

/**
 * Represents the result of processing weather data for UI consumption.
 *
 * This data class encapsulates the necessary information to update the UI state.
 *
 * @property uiState The state of the weather UI (e.g., [WeatherUiState.Loading], [WeatherUiState.Success], [WeatherUiState.Error]).
 */
data class WeatherProcessingResult(
    val uiState: WeatherUiState<CurrentWeatherUi>?,
)