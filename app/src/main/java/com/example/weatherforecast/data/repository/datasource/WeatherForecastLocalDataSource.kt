package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * Local data source interface
 */
interface WeatherForecastLocalDataSource {
    /**
     * Download weather forecast from a local storage, having [city] as a request parameter
     */
    suspend fun loadForecastData(city: String): WeatherForecastDomainModel

    /**
     * Save weather forecast data to local storage as a whole [response]
     */
    suspend fun saveForecastData(response: WeatherForecastDomainModel)
}