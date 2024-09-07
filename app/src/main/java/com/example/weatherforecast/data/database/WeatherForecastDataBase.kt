package com.example.weatherforecast.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import com.example.weatherforecast.models.data.WeatherForecastResponse

/**
 * Database for weather forecast
 */
@Database(entities = [WeatherForecastResponse::class, WeatherForecastCityResponse::class], version = 4)
abstract class WeatherForecastDataBase : RoomDatabase() {
    abstract fun getCitiesNamesInstance(): CitiesNamesDAO
    abstract fun getWeatherForecastInstance(): WeatherForecastDAO

    companion object {
        @Volatile
        private var INSTANCE: WeatherForecastDataBase? = null

        fun getInstance(context: Context): WeatherForecastDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherForecastDataBase::class.java,
                    "weather_forecast_database"
                )
                    // .fallbackToDestructiveMigration()
                    // .addMigrations(...) // Implement migrations if needed
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}