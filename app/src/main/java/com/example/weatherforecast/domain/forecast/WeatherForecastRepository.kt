package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType

/**
 * Weather forecast repository.
 * Provides domain-layer data.
 */
interface WeatherForecastRepository {

    /**
     * Retrieve remote weather forecast model for [temperatureType] and [city]
     */
    suspend fun loadRemoteForecastForCity(temperatureType: TemperatureType, city: String): WeatherForecastDomainModel

    /**
     * Retrieve remote weather forecast model for [temperatureType] and [latitude], [longitude]
     */
    suspend fun loadRemoteForecastForLocation(temperatureType: TemperatureType, latitude: Double, longitude: Double)
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