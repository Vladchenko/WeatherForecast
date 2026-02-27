package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.DataResult
import com.example.weatherforecast.models.data.network.HourlyWeatherDto
import kotlinx.serialization.InternalSerializationApi

/**
 * Remote(network) data source interface.
 */
interface HourlyWeatherRemoteDataSource {

    /**
     * Receive hourly weather forecast for [city].
     */
    @InternalSerializationApi
    suspend fun loadHourlyWeatherForCity(city: String): DataResult<HourlyWeatherDto>

    /**
     * Receive hourly weather forecast for a location defined by [latitude] and [longitude].
     */
    @InternalSerializationApi
    suspend fun loadHourlyWeatherForLocation(latitude: Double, longitude: Double): DataResult<HourlyWeatherDto>
}