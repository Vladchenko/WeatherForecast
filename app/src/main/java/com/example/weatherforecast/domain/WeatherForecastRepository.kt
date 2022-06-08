package com.example.weatherforecast.domain

import com.example.weatherforecast.data.models.WeatherForecastResponse
import com.example.weatherforecast.data.util.Resource

/**
 * Weather forecast repository.
 * Provides data-layer data.
 */
interface WeatherForecastRepository {
    suspend fun getWeatherForecastData(city: String): Resource<WeatherForecastResponse>
}