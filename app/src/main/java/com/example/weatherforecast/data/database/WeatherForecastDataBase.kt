package com.example.weatherforecast.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weatherforecast.data.models.domain.CityDomainModel
import com.example.weatherforecast.data.models.domain.WeatherForecastDomainModel

/**
 * Database for weather forecast
 */
@Database(entities = [WeatherForecastDomainModel::class, CityDomainModel::class], version = 1)
abstract class WeatherForecastDataBase : RoomDatabase() {
    abstract fun getCitiesNamesInstance(): CitiesNamesDAO
    abstract fun getWeatherForecastInstance(): WeatherForecastDAO
}