package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.DataResult
import com.example.weatherforecast.models.data.network.CurrentWeatherDto
import kotlinx.serialization.InternalSerializationApi

/**
 * Remote(network) data source interface.
 */
interface CurrentWeatherRemoteDataSource {

    /**
     * Receive weather forecast for [city].
     */
    @InternalSerializationApi
    suspend fun loadWeatherForCity(city: String): DataResult<CurrentWeatherDto>

    /**
     * Receive weather forecast for a location defined by [latitude] and [longitude].
     */
    @InternalSerializationApi
    suspend fun loadWeatherForLocation(
        latitude: Double,
        longitude: Double
    ): DataResult<CurrentWeatherDto>
}