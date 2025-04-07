package com.example.weatherforecast.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.weatherforecast.data.database.CitiesNamesDAO
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.database.WeatherForecastDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing persistence-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @Provides
    @Singleton
    fun provideWeatherForecastDatabase(
        @ApplicationContext context: Context
    ): WeatherForecastDatabase {
        return Room.databaseBuilder(
            context,
            WeatherForecastDatabase::class.java,
            "weather_forecast_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWeatherForecastDAO(database: WeatherForecastDatabase): WeatherForecastDAO {
        return database.getWeatherForecastInstance()
    }

    @Provides
    @Singleton
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