package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.data.models.data.WeatherForecastCityResponse
import retrofit2.Response

/**
 * Data source for cities names retrieval.
 */
interface CitiesNamesDataSource {
    /**
     * Retrieve cities names for [city] token.
     */
    suspend fun getCityNamesForTyping(city: String): Response<List<WeatherForecastCityResponse>>
}