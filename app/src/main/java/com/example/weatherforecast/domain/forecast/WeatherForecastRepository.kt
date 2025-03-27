package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * Weather forecast repository. Provides domain-layer data.
 */
interface WeatherForecastRepository {

    /**
     * Retrieve weather forecast model for [temperatureType] and [city] and save it to database
     *
     * @return result with data model
     */
    suspend fun loadAndSaveRemoteForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): LoadResult<WeatherForecastDomainModel>

    /**
     * Retrieve remote weather forecast model for [temperatureType] and [latitude], [longitude]
     *
     * @return result with data model
     */
    suspend fun loadAndSaveRemoteForecastForLocation(
        temperatureType: TemperatureType,
        latitude: Double,
        longitude: Double
    ): LoadResult<WeatherForecastDomainModel>

    /**
     * Retrieve local(database) forecast for [city] and provide [remoteError] describing why remote forecast failed
     */
    suspend fun loadLocalForecast(
        city: String,
        remoteError: String
    ): LoadResult<WeatherForecastDomainModel>

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