package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.CurrentWeatherResponse
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * Remote(network) data source interface.
 */
interface CurrentWeatherRemoteDataSource {

    /**
     * Receive weather forecast for [city].
     */
    @InternalSerializationApi
    suspend fun loadWeatherForCity(city: String): Response<CurrentWeatherResponse>

    /**
     * Receive weather forecast for a location defined by [latitude] and [longitude].
     */
    @InternalSerializationApi
    suspend fun loadWeatherForLocation(
        latitude: Double,
        longitude: Double
    ): Response<CurrentWeatherResponse>
}