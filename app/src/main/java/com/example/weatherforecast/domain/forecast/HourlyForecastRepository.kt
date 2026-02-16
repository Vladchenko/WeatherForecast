package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.LoadResult

/**
 * Weather hourly forecast repository. Provides domain-layer data.
 */
interface HourlyForecastRepository {

    /**
     * Retrieve local(database) forecast for [city] and provide [remoteError] describing why remote forecast failed
     */
    suspend fun loadLocalForecast(
        city: String,
        temperatureType: TemperatureType,
        remoteError: String
    ): LoadResult<HourlyForecastDomainModel>

    /**
     * Retrieve hourly weather forecast for [city]
     */
    suspend fun loadHourlyForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<HourlyForecastDomainModel>

    /**
     * Retrieve hourly weather forecast for location defined by [latitude] and [longitude]
     */
    suspend fun loadHourlyForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyForecastDomainModel>
}