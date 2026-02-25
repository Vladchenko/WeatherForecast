package com.example.weatherforecast.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherforecast.models.data.database.CitySearchEntity
import com.example.weatherforecast.models.data.database.CurrentWeatherEntity
import com.example.weatherforecast.models.data.database.HourlyWeatherEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * Main Room database for the weather forecast application.
 *
 * This database holds data across three core entities:
 * - [CurrentWeatherEntity]: stores current weather for cities
 * - [CitySearchEntity]: keeps track of previously searched cities
 * - [HourlyWeatherEntity]: contains hourly forecast data
 *
 * The database version is currently set to 6, with destructive migration enabled
 * to ensure schema updates do not cause crashes during development.
 *
 * @see CurrentWeatherDAO for current weather operations
 * @see CitiesNamesDAO for city search history
 * @see HourlyWeatherDAO for hourly forecasts
 */
@Database(
    entities = [CurrentWeatherEntity::class, CitySearchEntity::class, HourlyWeatherEntity::class],
    version = 6,
    exportSchema = false
)
@InternalSerializationApi
abstract class WeatherForecastDatabase : RoomDatabase() {

    /**
     * Provides access to city search data operations.
     *
     * @return An implementation of [CitiesNamesDAO]
     */
    abstract fun getCitiesNamesInstance(): CitiesNamesDAO

    /**
     * Provides access to current weather forecast data operations.
     *
     * @return An implementation of [CurrentWeatherDAO]
     */
    abstract fun getWeatherForecastInstance(): CurrentWeatherDAO

    /**
     * Provides access to hourly weather forecast data operations.
     *
     * @return An implementation of [HourlyWeatherDAO]
     */
    abstract fun getHourlyForecastInstance(): HourlyWeatherDAO

    /**
     * Singleton holder for [WeatherForecastDatabase].
     *
     * Ensures that only one instance of the database is created and used throughout the app.
     * Uses double-checked locking pattern to ensure thread safety.
     *
     * @param context Application context to create the database
     * @return Singleton instance of [WeatherForecastDatabase]
     */
    companion object {
        @Volatile
        private var INSTANCE: WeatherForecastDatabase? = null

        fun getInstance(context: Context): WeatherForecastDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherForecastDatabase::class.java,
                    "weather_forecast_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}