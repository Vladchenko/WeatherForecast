package io.github.vladchenko.weatherforecast.feature.currentweather.domain

import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.model.TemperatureType
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.models.CurrentWeather

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