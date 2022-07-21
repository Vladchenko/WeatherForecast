package com.example.weatherforecast.domain

import com.example.weatherforecast.data.models.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType

/**
 * Weather forecast repository.
 * Provides domain-layer data.
 */
interface WeatherForecastRepository {
    suspend fun loadRemoteForecast(temperatureType: TemperatureType, city: String): WeatherForecastDomainModel
    suspend fun loadLocalForecast(city: String): WeatherForecastDomainModel
    suspend fun saveForecast(model: WeatherForecastDomainModel)
}