package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.database.HourlyWeatherDAO
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import com.example.weatherforecast.models.data.database.HourlyWeatherEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * [CurrentWeatherRemoteDataSource] implementation.
 *
 * @property dao of Retrofit library to download weather forecast data
 */
@InternalSerializationApi
class HourlyWeatherLocalDataSourceImpl(private val dao: HourlyWeatherDAO) : HourlyWeatherLocalDataSource {

    @InternalSerializationApi
    override suspend fun getHourlyWeather(city: String): HourlyWeatherEntity {
        val entry = dao.getHourlyForecast(city) ?: throw NoSuchDatabaseEntryException(city)
        Log.d("HourlyWeatherLocalDataSourceImpl", "$city city forecast loaded successfully")
        return entry
    }

    @InternalSerializationApi
    override suspend fun saveHourlyWeather(entity: HourlyWeatherEntity) {
        dao.insertHourlyForecast(entity)
        Log.d("HourlyForecastLocalDataSourceImpl", "${entity.cityName} hourly weather saved successfully")
    }
}