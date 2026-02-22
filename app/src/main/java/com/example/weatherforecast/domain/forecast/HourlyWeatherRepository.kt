package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.ForecastError
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
        remoteError: ForecastError
    ): LoadResult<HourlyWeatherDomainModel>

    /**
     * Retrieve hourly weather forecast for [city], providing [temperatureType]
     */
    suspend fun refreshWeatherForCity(
        city: String,
        temperatureType: TemperatureType
    ): LoadResult<HourlyWeatherDomainModel>

    /**
     * Retrieve hourly weather forecast for location defined by [latitude] and [longitude]
     */
    suspend fun refreshWeatherForLocation(
        city: String,
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<HourlyWeatherDomainModel>
}