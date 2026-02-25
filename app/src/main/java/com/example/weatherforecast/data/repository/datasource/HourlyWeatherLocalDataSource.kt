package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.database.HourlyWeatherEntity

/**
 * Local data source interface
 */
/**
 * Interface for local data source providing access to cached hourly weather data.
 *
 * Implemented by Room database via DAO.
 */
interface HourlyWeatherLocalDataSource {

    /**
     * Retrieves main entity with city metadata by city name.
     *
     * @param city Name of the city
     * @return [HourlyWeatherEntity] if found, null otherwise
     */
    suspend fun getHourlyWeather(city: String): HourlyWeatherEntity?

    /**
     * Saves or updates main weather header and associated hourly items.
     *
     * Uses REPLACE strategy for conflict resolution.
     *
     * @param entity Complete entity containing city metadata and list of forecasts
     */
    suspend fun saveHourlyWeather(entity: HourlyWeatherEntity)
}