package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * Weather forecast repository.
 * Provides domain-layer data.
 */
interface WeatherForecastRepository {

    /**
     * Retrieve weather forecast model for [temperatureType] and [city]
     */
    suspend fun loadForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): Result<WeatherForecastDomainModel>

    /**
     * Retrieve weather forecast model for [temperatureType] and [latitude], [longitude]
     */
    suspend fun loadForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): Result<WeatherForecastDomainModel>

    /**
     * Retrieve remote weather forecast model for [temperatureType] and [city]
     */
    suspend fun loadRemoteForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): WeatherForecastDomainModel

    /**
     * Retrieve remote weather forecast model for [temperatureType] and [latitude], [longitude]
     */
    suspend fun loadRemoteForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    )
            : WeatherForecastDomainModel

    /**
     * Retrieve local(database) forecast for [city]
     */
    suspend fun loadLocalForecast(city: String): WeatherForecastDomainModel

    /**
     * Save weather forecast from [model]
     */
    suspend fun saveForecast(model: WeatherForecastDomainModel)
}