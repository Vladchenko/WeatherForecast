package io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource

import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * Local data source interface
 */
interface CurrentWeatherLocalDataSource {
    /**
     * Download weather forecast from a local storage, having [city] as a request parameter
     */
    @InternalSerializationApi
    suspend fun loadWeather(city: String): CurrentWeatherEntity

    /**
     * Save weather forecast data to local storage as a whole [response]
     */
    @InternalSerializationApi
    suspend fun saveWeather(response: CurrentWeatherEntity)
}