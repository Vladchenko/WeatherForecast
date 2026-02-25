package com.example.weatherforecast.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weatherforecast.models.data.database.CurrentWeatherEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.InternalSerializationApi

/**
 * Data Access Object for managing current weather forecasts in the local database.
 *
 * Provides methods to insert, update, delete, and query [CurrentWeatherEntity] objects
 * using Room persistence library. Supports both one-shot operations and reactive data streams.
 *
 * All suspend functions must be called from a coroutine context. Queries returning [Flow]
 * emit new data whenever the underlying database changes, making them suitable for UI observation.
 *
 * The table name `citiesForecasts` is defined in the [CurrentWeatherEntity] class.
 */
@Dao
interface CurrentWeatherDAO {

    /**
     * Inserts a new weather forecast into the database.
     *
     * If a conflict occurs (e.g., a forecast for the same city already exists),
     * the existing record is replaced due to [OnConflictStrategy.REPLACE].
     * Uses the primary key and field values from [model] to determine the conflict.
     *
     * @return The row ID of the inserted [CurrentWeatherEntity].
     */
    @InternalSerializationApi
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCityForecast(model: CurrentWeatherEntity): Long

    /**
     * Updates an existing weather forecast in the database.
     *
     * The record is matched by its primary key. Only fields mapped in [CurrentWeatherEntity]
     * are updated using values from [model].
     *
     * @return The number of rows updated (typically 1 if found, 0 otherwise).
     */
    @Update
    @InternalSerializationApi
    suspend fun updateCityForecast(model: CurrentWeatherEntity): Int

    /**
     * Deletes a specific weather forecast from the database.
     *
     * Uses the primary key of the provided [model] to locate the record.
     *
     * @return The number of deleted rows (1 if found, 0 otherwise).
     */
    @Delete
    @InternalSerializationApi
    suspend fun deleteCityForecast(model: CurrentWeatherEntity): Int

    /**
     * Removes all weather forecasts from the database.
     *
     * This operation deletes every record in the `citiesForecasts` table.
     *
     * @return The total number of deleted rows.
     */
    @InternalSerializationApi
    @Query("DELETE FROM citiesForecasts")
    suspend fun deleteAll(): Int

    /**
     * Retrieves the current weather forecast for a specific city by name.
     *
     * Returns `null` if no forecast is found for [city]. This method blocks the calling thread
     * and should be used with coroutines when called from background threads.
     *
     * Note: For reactive use in UI layers, prefer [getAllCitiesForecasts].
     */
    @InternalSerializationApi
    @Query("SELECT * FROM citiesForecasts WHERE city = :city")
    fun getCityForecast(city: String): CurrentWeatherEntity?

    /**
     * Retrieves all stored weather forecasts as a cold [Flow].
     *
     * Emits a fresh list of [CurrentWeatherEntity] every time the data in the database changes.
     * Ideal for observing live updates in ViewModels or Compose UI.
     */
    @InternalSerializationApi
    @Query("SELECT * FROM citiesForecasts")
    fun getAllCitiesForecasts(): Flow<List<CurrentWeatherEntity>>
}