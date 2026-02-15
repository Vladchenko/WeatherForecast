package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.HourlyForecastResponse
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * Remote(network) data source interface.
 */
interface HourlyForecastRemoteDataSource {

    /**
     * Receive hourly weather forecast for [city].
     */
    @InternalSerializationApi
    suspend fun loadHourlyForecastForCity(city: String): Response<HourlyForecastResponse>

    /**
     * Receive hourly weather forecast for a location defined by [latitude] and [longitude].
     */
    @InternalSerializationApi
    suspend fun loadHourlyForecastForLocation(latitude: Double, longitude: Double): Response<HourlyForecastResponse>
}