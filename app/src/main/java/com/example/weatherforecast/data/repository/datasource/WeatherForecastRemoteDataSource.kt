package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.WeatherForecastResponse
import retrofit2.Response

/**
 * Remote(network) data source interface.
 */
interface WeatherForecastRemoteDataSource {

    /**
     * Receive weather forecast for [city].
     */
    suspend fun loadForecastDataForCity(city: String): Response<WeatherForecastResponse>

    /**
     * Receive weather forecast for a location defined by [latitude] and [longitude].
     */
    suspend fun loadForecastForLocation(latitude: Double, longitude: Double): Response<WeatherForecastResponse>
}