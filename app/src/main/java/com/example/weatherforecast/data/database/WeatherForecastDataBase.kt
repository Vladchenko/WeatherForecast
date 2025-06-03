package com.example.weatherforecast.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherforecast.models.data.HourlyForecastResponse
import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import com.example.weatherforecast.models.data.WeatherForecastResponse

/**
 * Room database for storing weather forecast data.
 * This database contains tables for weather forecasts and city names.
 */
@Database(
    entities = [WeatherForecastResponse::class, WeatherForecastCityResponse::class, HourlyForecastResponse::class],
    version = 5
)
abstract class WeatherForecastDatabase : RoomDatabase() {
    abstract fun getCitiesNamesInstance(): CitiesNamesDAO
    abstract fun getWeatherForecastInstance(): WeatherForecastDAO
    abstract fun getHourlyForecastInstance(): HourlyForecastDAO

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