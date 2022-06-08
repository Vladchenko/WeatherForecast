package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.data.models.WeatherForecastResponse
import retrofit2.Response

/**
 * Data source interface
 */
interface WeatherForecastDataSource {
    /**
     * Receives data, having [city] as a request parameter
     */
    suspend fun getWeatherForecastData(city: String): Response<WeatherForecastResponse>
}