package com.example.weatherforecast.presentation.viewmodel.forecast

import com.example.weatherforecast.models.domain.WeatherForecast

/**
 * UI states for weather forecast screen
 */
sealed class ForecastUiState {
    object Loading: ForecastUiState()
    data class Success(val forecast: WeatherForecast,
                       val source: DataSource): ForecastUiState()
    data class Error(val message: String): ForecastUiState()
}

enum class DataSource {
    REMOTE, LOCAL
}