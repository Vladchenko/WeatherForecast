package io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.repository.datasource

import io.github.vladchenko.weatherforecast.core.data.models.DataResult
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.model.HourlyWeatherDto
import kotlinx.serialization.InternalSerializationApi

/**
 * Remote(network) data source interface.
 */
interface HourlyWeatherRemoteDataSource {

    /**
     * Load hourly weather forecast for [city].
     */
    @InternalSerializationApi
    suspend fun loadHourlyWeatherForCity(city: String): DataResult<HourlyWeatherDto>

    /**
     * Load hourly weather forecast for a location defined by [latitude] and [longitude].
     * [city] is to inform a user for case when loading fails.
     */
    @InternalSerializationApi
    suspend fun loadHourlyWeatherForLocation(
        city: String,
        latitude: Double,
        longitude: Double
    ): DataResult<HourlyWeatherDto>
}