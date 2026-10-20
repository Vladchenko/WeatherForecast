package io.github.vladchenko.weatherforecast.data.repository

import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.HourlyWeather
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit

/**
 * Hourly weather forecast repository. Provides data at the domain level.
 */
interface HourlyWeatherRepository {

    /**
     * Fetches local (database) forecast for [city] and provides [remoteError] describing
     * why the remote forecast failed.
     *
     * Used to return cached data when the remote request fails.
     *
     * @param city city name to fetch forecast for
     * @param temperatureUnit temperature unit
     * @param remoteError error that occurred during remote fetch
     * @return [LoadResult] containing cached data or error
     */
    suspend fun loadCachedWeather(
        city: String,
        temperatureUnit: TemperatureUnit,
        remoteError: ForecastError
    ): LoadResult<HourlyWeather>

    /**
     * Fetches hourly weather forecast for the location defined by [latitude] and [longitude].
     *
     * Performs loading of fresh data from the remote source with saving to local cache.
     *
     * @param city city name
     * @param latitude geographic latitude
     * @param longitude geographic longitude
     * @param temperatureUnit temperature unit
     * @return [LoadResult] indicating data source (Remote, Local) or error
     */
    suspend fun refreshWeatherForLocation(
        city: String,
        latitude: Double,
        longitude: Double,
        temperatureUnit: TemperatureUnit
    ): LoadResult<HourlyWeather>
}