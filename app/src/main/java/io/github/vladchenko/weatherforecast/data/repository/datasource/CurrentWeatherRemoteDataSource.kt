package io.github.vladchenko.weatherforecast.data.repository.datasource

import io.github.vladchenko.weatherforecast.core.data.models.DataResult
import io.github.vladchenko.weatherforecast.models.data.network.CurrentWeatherDto
import kotlinx.serialization.InternalSerializationApi

/**
 * Remote(network) data source interface.
 */
interface CurrentWeatherRemoteDataSource {

    /**
     * Load weather forecast for a location defined by [latitude] and [longitude] and [city]
     * for loading-fail case to inform user .
     */
    @InternalSerializationApi
    suspend fun loadWeatherForLocation(
        city: String,
        latitude: Double,
        longitude: Double
    ): DataResult<CurrentWeatherDto>
}