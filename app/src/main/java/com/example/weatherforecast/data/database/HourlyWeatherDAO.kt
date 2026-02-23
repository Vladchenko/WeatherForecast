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
@InternalSerializationApi
interface HourlyWeatherDAO {

    @Query("SELECT * FROM hourlyForecasts WHERE city_name = :cityName")
    suspend fun getHourlyForecast(cityName: String): HourlyWeatherResponse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecast(forecast: HourlyWeatherResponse)
}