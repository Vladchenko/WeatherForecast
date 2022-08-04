package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.data.models.data.WeatherForecastCityResponse
import retrofit2.Response

/**
 * Data source for cities names retrieval from network.
 */
interface CitiesNamesRemoteDataSource {
    /**
     * Retrieve cities names for [token].
     */
    suspend fun getCityNames(token: String): Response<List<WeatherForecastCityResponse>>
}