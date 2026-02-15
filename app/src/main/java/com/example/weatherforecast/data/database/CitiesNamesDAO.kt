package com.example.weatherforecast.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import kotlinx.serialization.InternalSerializationApi

/**
 * Cities names DAO class for ROOM database.
 */
@Dao
interface CitiesNamesDAO {

    @InternalSerializationApi
    @Insert(onConflict = OnConflictStrategy.REPLACE)    // Replaces a new entity with an old one, when a conflict arises
    suspend fun insertCityName(cityName: WeatherForecastCityResponse): Long

    @InternalSerializationApi
    @Update
    suspend fun updateCityName(cityName: WeatherForecastCityResponse): Int   // Returns a number of rows updated

    @InternalSerializationApi
    @Delete
    suspend fun deleteCityName(cityName: WeatherForecastCityResponse): Int   // Returns a number of rows deleted

    @Query("DELETE FROM citiesNames")
    suspend fun deleteAll(): Int

    @InternalSerializationApi
    @Query("SELECT * FROM citiesNames WHERE city MATCH " + ":token")
    fun getCitiesNames(token: String): List<WeatherForecastCityResponse>

    @InternalSerializationApi
    @Query("SELECT * FROM citiesNames")
    fun getAllCitiesNames(): List<WeatherForecastCityResponse> // Flow is used to get several items
}