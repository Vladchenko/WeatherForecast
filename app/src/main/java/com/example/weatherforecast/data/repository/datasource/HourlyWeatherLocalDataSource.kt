package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.HourlyWeatherResponse
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * Local data source interface
 */
interface HourlyWeatherLocalDataSource {
    /**
     * Download weather forecast from a local storage, having [city] as a request parameter
     */
    @InternalSerializationApi
    suspend fun getHourlyWeather(city: String): Response<HourlyWeatherResponse>

    /**
     * Save weather forecast data to local storage as a whole [response]
     */
    @InternalSerializationApi
    suspend fun saveHourlyWeather(response: HourlyWeatherResponse)
}