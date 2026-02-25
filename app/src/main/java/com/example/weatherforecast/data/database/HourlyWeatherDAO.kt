package com.example.weatherforecast.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.models.data.database.HourlyWeatherEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * Data Access Object for managing hourly weather forecasts in the local database.
 *
 * Provides methods to insert and retrieve [HourlyWeatherEntity] objects using Room.
 * Designed to store time-series forecast data for cities, allowing efficient lookups by city name.
 *
 * All operations are suspend functions and must be called from a coroutine context.
 *
 * @see HourlyWeatherEntity
 */
@Dao
@InternalSerializationApi
interface HourlyWeatherDAO {

    /**
     * Retrieves the hourly forecast for a specific city.
     *
     * Returns `null` if no forecast is found for [cityName].
     *
     * @param cityName The name of the city to query.
     * @return The corresponding [HourlyWeatherEntity], or `null` if not found.
     */
    @Query("SELECT * FROM hourlyForecasts WHERE cityName = :cityName")
    suspend fun getHourlyForecast(cityName: String): HourlyWeatherEntity?

    /**
     * Inserts or replaces an hourly forecast in the database.
     *
     * If a conflict occurs (e.g., a forecast for the same city already exists),
     * it is replaced due to [OnConflictStrategy.REPLACE].
     *
     * @param forecast The [HourlyWeatherEntity] to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecast(forecast: HourlyWeatherEntity)
}