package com.example.weatherforecast.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * Database for weather forecast
 */
@Database(entities = [WeatherForecastDomainModel::class, CityDomainModel::class], version = 2)
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
                    // .addMigrations(...) // Implement migrations if needed
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}