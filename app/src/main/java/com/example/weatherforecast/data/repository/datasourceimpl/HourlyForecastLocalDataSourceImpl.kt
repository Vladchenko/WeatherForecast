package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.database.HourlyForecastDAO
import com.example.weatherforecast.data.repository.datasource.HourlyForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.models.data.HourlyForecastResponse
import kotlinx.serialization.InternalSerializationApi

/**
 * [WeatherForecastRemoteDataSource] implementation.
 *
 * @property dao of Retrofit library to download weather forecast data
 */
class HourlyForecastLocalDataSourceImpl(private val dao: HourlyForecastDAO) : HourlyForecastLocalDataSource {

    @InternalSerializationApi
    override suspend fun getHourlyForecastData(city: String): HourlyForecastResponse {
        Log.d("HourlyForecastLocalDataSourceImpl", "$city city forecast loaded successfully")
        val entry = dao.getHourlyForecast(city) ?: throw NoSuchDatabaseEntryException(city)
        return entry
    }

    @InternalSerializationApi
    override suspend fun saveHourlyForecastData(response: HourlyForecastResponse) {
        dao.insertHourlyForecast(response)
        Log.d("HourlyForecastLocalDataSourceImpl", "${response.city} city forecast saved successfully")
    }
}