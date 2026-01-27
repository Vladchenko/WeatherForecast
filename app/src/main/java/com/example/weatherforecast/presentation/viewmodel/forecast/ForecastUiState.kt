package com.example.weatherforecast.presentation.viewmodel.forecast

import com.example.weatherforecast.models.presentation.WeatherForecastUi

/**
 * UI states for weather forecast screen
 */
sealed class ForecastUiState {
    object Loading: ForecastUiState()
    data class Success(val forecast: WeatherForecastUi,
                       val source: DataSource): ForecastUiState()
    data class Error(val message: String): ForecastUiState()
}

enum class DataSource {
    REMOTE, LOCAL
}