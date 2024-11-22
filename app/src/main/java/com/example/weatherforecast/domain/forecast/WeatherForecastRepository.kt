package com.example.weatherforecast.domain.forecast

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.domain.LoadResult
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
    suspend fun loadRemoteForecastForCity(
        temperatureType: TemperatureType,
        city: String
    ): Result<WeatherForecastDomainModel>

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
}