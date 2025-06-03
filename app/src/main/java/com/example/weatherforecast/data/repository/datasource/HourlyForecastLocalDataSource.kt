package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.HourlyForecastResponse

/**
 * Local data source interface
 */
interface HourlyForecastLocalDataSource {
    /**
     * Download weather forecast from a local storage, having [city] as a request parameter
     */
    suspend fun getHourlyForecastData(city: String): HourlyForecastResponse

    /**
     * Save weather forecast data to local storage as a whole [response]
     */
    suspend fun saveHourlyForecastData(response: HourlyForecastResponse)
}