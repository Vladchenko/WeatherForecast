package com.example.weatherforecast.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.models.data.HourlyForecastResponse
import kotlinx.serialization.InternalSerializationApi

/**
 * DAO interface for handling hourly forecast data in the local database
 */
@Dao
interface HourlyForecastDAO {
    @InternalSerializationApi
    @Query("SELECT * FROM hourlyForecasts WHERE city = :city")
    suspend fun getHourlyForecast(city: String): HourlyForecastResponse?

    @InternalSerializationApi
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecast(forecast: HourlyForecastResponse)
} 