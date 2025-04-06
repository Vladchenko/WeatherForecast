package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.HourlyForecastResponse
import retrofit2.Response

/**
 * Remote(network) data source interface.
 */
interface HourlyForecastRemoteDataSource {

    /**
     * Receive hourly weather forecast for [city].
     */
    suspend fun loadHourlyForecastForCity(city: String): Response<HourlyForecastResponse>

    /**
     * Receive hourly weather forecast for a location defined by [latitude] and [longitude].
     */
    suspend fun loadHourlyForecastForLocation(latitude: Double, longitude: Double): Response<HourlyForecastResponse>
}