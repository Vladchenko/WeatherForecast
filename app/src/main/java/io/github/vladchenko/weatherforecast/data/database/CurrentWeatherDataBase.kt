package io.github.vladchenko.weatherforecast.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchEntity
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherEntity
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.model.HourlyWeatherEntity
import io.github.vladchenko.weatherforecast.feature.recentcities.data.model.RecentCitiesEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * Main Room database for the weather forecast application.
 *
 * This database holds data across these core entities:
 * - [CurrentWeatherEntity]: stores current weather for cities
 * - [CitySearchEntity]: keeps track of previously searched cities
 * - [HourlyWeatherEntity]: contains hourly forecast data
 * - [RecentCitiesEntity]: keeps recently used cities
 *
 * The database version is currently set to 8, with destructive migration enabled
 * to ensure schema updates do not cause crashes during development.
 */
@Database(
    entities = [CurrentWeatherEntity::class, CitySearchEntity::class, HourlyWeatherEntity::class, RecentCitiesEntity::class],
    version = 8,
    exportSchema = false
)
@InternalSerializationApi
abstract class WeatherForecastDatabase : RoomDatabase() {

    /**
     * Provides access to city search data operations.
     *
     * @return An implementation of [CitySearchDAO]
     */
    abstract fun getCitiesNamesInstance(): CitySearchDAO

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
     * Provides a list of cities, recently provided forecast for
     *
     * @return An implementation of [RecentCitiesDAO]
     */
    abstract fun getRecentCitiesDao(): RecentCitiesDAO

    /**
     * Singleton instance holder for the [WeatherForecastDatabase].
     *
     * This companion object ensures that only one instance of the database is ever created,
     * following the singleton pattern with thread-safe initialization using double-checked locking.
     * The instance is retained across configuration changes and shared across all components
     * that access the database.
     */
    companion object {
        @Volatile
        private var INSTANCE: WeatherForecastDatabase? = null

        /**
         * Returns the singleton instance of [WeatherForecastDatabase], creating it if necessary.
         *
         * Uses [Room.databaseBuilder] with the application context to ensure lifecycle independence.
         * Applies destructive migration as a development-friendly strategy — **not recommended for production**
         * unless paired with proper migration scripts.
         *
         * Thread-safe via `synchronized` block and `@Volatile` annotation on the instance field.
         *
         * @param context Application context used to build the database
         * @return Singleton instance of [WeatherForecastDatabase]
         */
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