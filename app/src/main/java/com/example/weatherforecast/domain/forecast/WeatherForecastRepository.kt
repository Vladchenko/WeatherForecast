package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * Weather forecast repository. Provides domain-layer data.
 */
interface WeatherForecastRepository {

    /**
     * Retrieve weather forecast model for [temperatureType] and [city]
     *
     * @return result with data model
     */
    suspend fun loadAndSaveRemoteForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): Result<WeatherForecastDomainModel>

    /**
     * Retrieve remote weather forecast model for [temperatureType] and [latitude], [longitude]
     *
     * @return result with data model
     */
    suspend fun loadRemoteForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): Result<WeatherForecastDomainModel>

    /**
     * Retrieve local(database) forecast for [city]
     */
    suspend fun loadLocalForecast(city: String): WeatherForecastDomainModel
}