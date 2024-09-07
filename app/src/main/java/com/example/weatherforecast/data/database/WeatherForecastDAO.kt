package com.example.weatherforecast.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weatherforecast.models.data.WeatherForecastResponse
import kotlinx.coroutines.flow.Flow

/**
 * Weather forecast DAO class for ROOM database
 */
@Dao
interface WeatherForecastDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)    // Replaces a new entity with an old one, when a conflict arises
    suspend fun insertCityForecast(model: WeatherForecastResponse): Long

    @Update
    suspend fun updateCityForecast(model: WeatherForecastResponse): Int   // Returns a number of rows updated

    @Delete
    suspend fun deleteCityForecast(model: WeatherForecastResponse): Int   // Returns a number of rows deleted

    @Query("DELETE FROM citiesForecasts") // Name of table is taken from WeatherForecastResponse.kt data class
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM citiesForecasts WHERE city = :city")
    fun getCityForecast(city: String): WeatherForecastResponse? // This is not a suspend fun, since it returns LiveData

    @Query("SELECT * FROM citiesForecasts")
    fun getAllCitiesForecasts(): Flow<List<WeatherForecastResponse>> // Flow is used to get several items
}