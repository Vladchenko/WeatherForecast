package com.example.weatherforecast.di

import android.content.Context
import android.content.SharedPreferences
import com.example.weatherforecast.data.database.CitiesNamesDAO
import com.example.weatherforecast.data.database.HourlyForecastDAO
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.database.WeatherForecastDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides persistence-related dependencies for the application.
 *
 * This object module is installed in the [SingletonComponent], ensuring that all provided instances
 * are created once and shared across the entire app lifecycle.
 *
 * It supplies:
 * - A singleton [WeatherForecastDatabase] instance using Room, which serves as the main database
 *   for storing weather forecasts, hourly data, and city names
 * - Data Access Objects (DAOs) for each entity:
 *   - [WeatherForecastDAO] – for current weather data
 *   - [HourlyForecastDAO] – for hourly forecast entries
 * - [CitiesNamesDAO] – for cached city name suggestions
 * - A [SharedPreferences] instance for lightweight persistent storage,
 *   used to save user preferences such as the selected city
 *
 * The database is initialized via [WeatherForecastDatabase.getInstance], which ensures a single instance
 * across the app and supports proper dependency injection.
 *
 * @see WeatherForecastDatabase
 * @see WeatherForecastDAO
 * @see HourlyForecastDAO
 * @see CitiesNamesDAO
 * @see SharedPreferences
 */
@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @Provides
    @Singleton
    @InternalSerializationApi
    fun provideWeatherForecastDatabase(
        @ApplicationContext context: Context
    ): WeatherForecastDatabase {
        return WeatherForecastDatabase.getInstance(context)
        // following code was present earlier,
//        Room.databaseBuilder(
//            context,
//            WeatherForecastDatabase::class.java,
//            "weather_forecast_database"
//        ).build()
    }

    @Provides
    @Singleton
    @InternalSerializationApi
    fun provideWeatherForecastDAO(database: WeatherForecastDatabase): WeatherForecastDAO {
        return database.getWeatherForecastInstance()
    }

    @Provides
    @Singleton
    @InternalSerializationApi
    fun provideHourlyForecastDAO(database: WeatherForecastDatabase): HourlyForecastDAO {
        return database.getHourlyForecastInstance()
    }

    @Provides
    @Singleton
    @InternalSerializationApi
    fun provideCitiesNamesDAO(database: WeatherForecastDatabase): CitiesNamesDAO {
        return database.getCitiesNamesInstance()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("weather_forecast_prefs", Context.MODE_PRIVATE)
    }
}