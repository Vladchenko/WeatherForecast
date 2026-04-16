package io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * City search DAO class for ROOM database.
 */
@Dao
interface CitySearchDAO {

    @InternalSerializationApi
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)    // Replaces an old entity with a new one, when a conflict arises
    suspend fun insertCitiesNames(citiesNames: List<CitySearchEntity>): LongArray

    @InternalSerializationApi
    @Update
    suspend fun updateCityName(cityName: CitySearchEntity): Int   // Returns a number of rows updated

    @InternalSerializationApi
    @Delete
    suspend fun deleteCityName(cityName: CitySearchEntity): Int   // Returns a number of rows deleted

    @Query("DELETE FROM citiesNames")
    suspend fun deleteAll(): Int

    @InternalSerializationApi
    @Query("SELECT * FROM citiesNames WHERE city LIKE '%' || :token || '%'")
    fun findCitiesNames(token: String): List<CitySearchEntity>

    @InternalSerializationApi
    @Query("SELECT * FROM citiesNames")
    fun findAllCitiesNames(): List<CitySearchEntity>
}