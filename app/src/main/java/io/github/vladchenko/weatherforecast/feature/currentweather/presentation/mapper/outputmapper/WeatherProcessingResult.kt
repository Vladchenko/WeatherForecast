package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.outputmapper

import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CityErrorEvent

/**
 * Represents the result of processing weather data for UI consumption.
 *
 * This data class encapsulates the necessary information to update the UI state,
 * persist city model changes, or handle city-specific errors.
 *
 * @property uiState The state of the weather UI (e.g., [WeatherUiState.Loading], [WeatherUiState.Success], [WeatherUiState.Error]).
 * @property cityError A specific error event related to the city, or `null` if no error occurred.
 */
data class WeatherProcessingResult(
    val uiState: WeatherUiState<CurrentWeatherUi>?,
    val cityError: CityErrorEvent?
)