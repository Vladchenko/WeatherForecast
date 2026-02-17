package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.CurrentWeatherResponse
import kotlinx.serialization.InternalSerializationApi

/**
 * Local data source interface
 */
interface CurrentWeatherLocalDataSource {
    /**
     * Download weather forecast from a local storage, having [city] as a request parameter
     */
    @InternalSerializationApi
    suspend fun loadWeather(city: String): CurrentWeatherResponse

    /**
     * Save weather forecast data to local storage as a whole [response]
     */
    @InternalSerializationApi
    suspend fun saveWeather(response: CurrentWeatherResponse)
}