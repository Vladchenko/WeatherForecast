package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * Data source for cities names retrieval from network.
 */
interface CitiesNamesRemoteDataSource {
    /**
     * Retrieve cities names for [token].
     */
    @InternalSerializationApi
    suspend fun loadCitiesNames(token: String): Response<List<WeatherForecastCityResponse>>
}