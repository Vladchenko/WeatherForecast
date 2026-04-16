package io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource

import io.github.vladchenko.weatherforecast.core.data.models.DataResult
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherDto
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