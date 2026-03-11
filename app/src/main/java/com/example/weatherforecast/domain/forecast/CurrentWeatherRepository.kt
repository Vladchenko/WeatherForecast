package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.CurrentWeather
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.LoadResult

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

    /**
     * Retrieve cached weather for [city] and provide [remoteError] describing why remote forecast failed
     */
    suspend fun loadCachedWeatherForCity(
        city: String,
        temperatureType: TemperatureType,
        remoteError: ForecastError
    ): LoadResult<CurrentWeather>
}