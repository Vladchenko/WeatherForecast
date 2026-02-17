package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.database.HourlyWeatherDAO
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherLocalDataSource
import com.example.weatherforecast.models.data.HourlyWeatherResponse
import kotlinx.serialization.InternalSerializationApi

/**
 * [CurrentWeatherRemoteDataSource] implementation.
 *
 * @property dao of Retrofit library to download weather forecast data
 */
class HourlyWeatherLocalDataSourceImpl(private val dao: HourlyWeatherDAO) : HourlyWeatherLocalDataSource {

    @InternalSerializationApi
    override suspend fun getHourlyWeather(city: String): HourlyWeatherResponse {
        Log.d("HourlyForecastLocalDataSourceImpl", "$city city forecast loaded successfully")
        val entry = dao.getHourlyForecast(city) ?: throw NoSuchDatabaseEntryException(city)
        return entry
    }

    @InternalSerializationApi
    override suspend fun saveHourlyWeather(response: HourlyWeatherResponse) {
        dao.insertHourlyForecast(response)
        Log.d("HourlyForecastLocalDataSourceImpl", "${response.city} city forecast saved successfully")
    }
}