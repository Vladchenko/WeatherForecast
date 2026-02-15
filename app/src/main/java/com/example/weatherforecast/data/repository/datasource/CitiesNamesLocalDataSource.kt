package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import kotlinx.serialization.InternalSerializationApi

/**
 * Data source for local cities names operations.
 */
interface CitiesNamesLocalDataSource {
    /**
     * Retrieve cities names for [token].
     */
    @InternalSerializationApi
    fun loadCitiesNames(token: String): List<WeatherForecastCityResponse>

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames()
}