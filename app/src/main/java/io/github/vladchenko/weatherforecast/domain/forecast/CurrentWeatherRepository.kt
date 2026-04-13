package io.github.vladchenko.weatherforecast.domain.forecast

import io.github.vladchenko.weatherforecast.data.util.TemperatureType
import io.github.vladchenko.weatherforecast.models.domain.CurrentWeather
import io.github.vladchenko.weatherforecast.models.domain.LoadResult

/**
 * Weather forecast repository. Provides domain-layer data.
 */
interface CurrentWeatherRepository {

    /**
     * Retrieve remote weather model for [temperatureType] and [latitude], [longitude]
     *
     * @return result with data model
     */
    suspend fun refreshWeatherForLocation(
        city: String,
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<CurrentWeather>
}