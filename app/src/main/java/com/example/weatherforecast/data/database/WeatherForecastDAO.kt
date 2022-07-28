package com.example.weatherforecast.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weatherforecast.data.models.domain.WeatherForecastDomainModel

/**
 * DAO class for ROOM database
 */
@Dao
interface WeatherForecastDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)    // Replaces a new entity with an old one, when a conflict arises
    suspend fun insertCityForecast(model: WeatherForecastDomainModel): Long

    @Update
    suspend fun updateCityForecast(model: WeatherForecastDomainModel): Int   // Returns a number of rows updated

    @Delete
    suspend fun deleteCityForecast(model: WeatherForecastDomainModel): Int   // Returns a number of rows deleted

    @Query("DELETE FROM citiesForecasts") // Name of table is taken from WeatherForecastDomainModel.kt data class
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM citiesForecasts WHERE city = " + ":city")
    fun getCityForecast(city: String): WeatherForecastDomainModel // This is not a suspend fun, since it returns LiveData

    @Query("SELECT * FROM citiesForecasts")
    fun getAllCitiesForecasts(): List<WeatherForecastDomainModel> // This is not a suspend fun, since it returns LiveData
    // // At the end of a lesson, there is saying - use Flow instead of LiveData, like Flow<List<WeatherForecastDomainModel>>

    // @Insert
    // fun insertSubscriber2(model: WeatherForecastDomainModel): Long  // This ordinary fun is used for an implementation different
    // // to a coroutines one, say AsyncTask/Executors/JavaRX
    //
    // @Insert
    // fun insertSubscriber2(model: List<WeatherForecastDomainModel>): List<Long>
}