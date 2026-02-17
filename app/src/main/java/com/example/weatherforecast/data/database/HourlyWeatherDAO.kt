package com.example.weatherforecast.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherforecast.models.data.HourlyWeatherResponse
import kotlinx.serialization.InternalSerializationApi

/**
 * DAO interface for handling hourly forecast data in the local database
 */
@Dao
interface HourlyWeatherDAO {
    @InternalSerializationApi
    @Query("SELECT * FROM hourlyForecasts WHERE city = :city")
    suspend fun getHourlyForecast(city: String): HourlyWeatherResponse?

    @InternalSerializationApi
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecast(forecast: HourlyWeatherResponse)
} 