package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import com.example.weatherforecast.models.domain.LoadResult

/**
 * Weather hourly forecast repository. Provides domain-layer data.
 */
interface HourlyWeatherRepository {

    /**
     * Retrieve local(database) forecast for [city] and provide [remoteError] describing why remote forecast failed
     */
    suspend fun loadCachedWeather(
        city: String,
        temperatureType: TemperatureType,
        remoteError: String
    ): LoadResult<HourlyWeatherDomainModel>

    /**
     * Retrieve hourly weather forecast for [city]
     */
    suspend fun refreshWeatherForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<HourlyWeatherDomainModel>

    /**
     * Retrieve hourly weather forecast for location defined by [latitude] and [longitude]
     */
    suspend fun refreshWeatherForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeatherDomainModel>
}