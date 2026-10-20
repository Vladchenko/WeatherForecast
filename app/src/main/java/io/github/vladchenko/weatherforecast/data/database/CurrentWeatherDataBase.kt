package io.github.vladchenko.weatherforecast.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.local.AISummaryDAO
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryEntity
import io.github.vladchenko.weatherforecast.feature.citysearch.data.datasource.local.CitySearchDAO
import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchEntity
import io.github.vladchenko.weatherforecast.feature.currentweather.data.dao.CurrentWeatherDAO
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherEntity
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.dao.HourlyWeatherDAO
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.data.model.HourlyWeatherEntity
import io.github.vladchenko.weatherforecast.feature.recentcities.data.dao.RecentCitiesDAO
import io.github.vladchenko.weatherforecast.feature.recentcities.data.model.RecentCitiesEntity

/**
 * Main Room database for the weather forecast application.
 *
 * This database stores data for the following entities:
 * - [CurrentWeatherEntity]: stores current weather for cities
 * - [CitySearchEntity]: tracks previously searched cities
 * - [HourlyWeatherEntity]: contains hourly forecast data
 * - [RecentCitiesEntity]: stores recently used cities
 * - [AISummaryEntity]: stores AI weather summaries
 *
 * Database version is set to 9, with destructive migration enabled,
 * which allows schema updates without crashes during development.
 */
@Database(
    entities = [CurrentWeatherEntity::class, CitySearchEntity::class, HourlyWeatherEntity::class, RecentCitiesEntity::class, AISummaryEntity::class],
    version = 11,
    exportSchema = false
)
abstract class WeatherForecastDatabase : RoomDatabase() {

    /**
     * Provides access to city search data operations.
     *
     * @return implementation of [CitySearchDAO]
     */
    abstract fun getCitySearchInstance(): CitySearchDAO

    /**
     * Provides access to current weather forecast data operations.
     *
     * @return implementation of [CurrentWeatherDAO]
     */
    abstract fun getWeatherForecastInstance(): CurrentWeatherDAO

    /**
     * Provides access to hourly weather forecast operations.
     *
     * @return implementation of [HourlyWeatherDAO]
     */
    abstract fun getHourlyForecastInstance(): HourlyWeatherDAO

    /**
     * Provides access to recently used cities with forecast.
     *
     * @return implementation of [RecentCitiesDAO]
     */
    abstract fun getRecentCitiesDao(): RecentCitiesDAO

    /**
     * Provides access to AI weather summary data.
     *
     * @return implementation of [AISummaryDAO]
     */
    abstract fun getAiDAO(): AISummaryDAO

    /**
     * Singleton holder for [WeatherForecastDatabase] instance.
     *
     * This companion object ensures that only one database instance is created,
     * following the singleton pattern with thread-safe initialization via double-checked locking.
     * The instance is preserved across configuration changes and shared among all components
     * accessing the database.
     */
    companion object {
        /**
         * Holds the singleton database instance. Initialized lazily with thread safety.
         */
        @Volatile
        private var INSTANCE: WeatherForecastDatabase? = null

        /**
         * Returns the singleton [WeatherForecastDatabase] instance, creating it if necessary.
         *
         * Uses [Room.databaseBuilder] with application context to ensure lifecycle independence.
         * Applies destructive migration as a development-friendly strategy — **not recommended for production**
         * without proper migration scripts.
         *
         * Thread safety is ensured through `synchronized` block and `@Volatile` annotation on the instance field.
         *
         * @param context application context used for creating the database
         * @return singleton instance of [WeatherForecastDatabase]
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