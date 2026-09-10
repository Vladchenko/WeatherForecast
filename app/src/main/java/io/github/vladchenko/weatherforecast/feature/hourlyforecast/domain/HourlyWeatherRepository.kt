package io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain

import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.HourlyWeather
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit

/**
 * Weather hourly forecast repository. Provides domain-layer data.
 */
interface HourlyWeatherRepository {

    /**
     * Retrieve local(database) forecast for [city] and provide [remoteError] describing why remote forecast failed
     */
    suspend fun loadCachedWeather(
        city: String,
        temperatureUnit: TemperatureUnit,
        remoteError: ForecastError
    ): LoadResult<HourlyWeather>

    /**
     * Retrieve hourly weather forecast for location defined by [latitude] and [longitude]
     */
    suspend fun refreshWeatherForLocation(
        city: String,
        temperatureUnit: TemperatureUnit,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeather>
}