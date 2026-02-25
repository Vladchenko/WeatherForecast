package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.database.CurrentWeatherEntity
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * Local data source interface
 */
interface CurrentWeatherLocalDataSource {
    /**
     * Download weather forecast from a local storage, having [city] as a request parameter
     */
    @InternalSerializationApi
    suspend fun loadWeather(city: String): Response<CurrentWeatherEntity>

    /**
     * Save weather forecast data to local storage as a whole [response]
     */
    @InternalSerializationApi
    suspend fun saveWeather(response: CurrentWeatherEntity)
}