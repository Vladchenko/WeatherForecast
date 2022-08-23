package com.example.weatherforecast.data.database

import androidx.room.*
import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Cities names DAO class for ROOM database.
 */
@Dao
interface CitiesNamesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)    // Replaces a new entity with an old one, when a conflict arises
    suspend fun insertCityName(cityName: CityDomainModel): Long

    @Update
    suspend fun updateCityName(cityName: CityDomainModel): Int   // Returns a number of rows updated

    @Delete
    suspend fun deleteCityName(cityName: CityDomainModel): Int   // Returns a number of rows deleted

    @Query("SELECT * FROM citiesNames WHERE name MATCH " + ":token")
    fun getCitiesNames(token: String): Flow<CityDomainModel> // Flow is used to get several items

    @Query("SELECT * FROM citiesNames")
    fun getAllCitiesNames(): Flow<CityDomainModel> // Flow is used to get several items
}