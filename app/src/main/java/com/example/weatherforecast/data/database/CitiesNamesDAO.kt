package com.example.weatherforecast.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weatherforecast.models.data.database.CitySearchEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * Cities names DAO class for ROOM database.
 */
@Dao
interface CitiesNamesDAO {

    @InternalSerializationApi
    @Insert(onConflict = OnConflictStrategy.REPLACE)    // Replaces an old entity with a new one, when a conflict arises
    suspend fun insertCityName(cityName: CitySearchEntity): Long

    @InternalSerializationApi
    @Update
    suspend fun updateCityName(cityName: CitySearchEntity): Int   // Returns a number of rows updated

    @InternalSerializationApi
    @Delete
    suspend fun deleteCityName(cityName: CitySearchEntity): Int   // Returns a number of rows deleted

    @Query("DELETE FROM citiesNames")
    suspend fun deleteAll(): Int

    @InternalSerializationApi
    @Query("SELECT * FROM citiesNames WHERE city MATCH " + ":token")
    fun getCitiesNames(token: String): List<CitySearchEntity>

    @InternalSerializationApi
    @Query("SELECT * FROM citiesNames")
    fun getAllCitiesNames(): List<CitySearchEntity>
}