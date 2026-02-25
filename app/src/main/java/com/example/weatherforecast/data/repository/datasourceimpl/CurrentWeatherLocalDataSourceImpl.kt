package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.database.CurrentWeatherDAO
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherLocalDataSource
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.models.data.database.CurrentWeatherEntity
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * [CurrentWeatherRemoteDataSource] implementation.
 *
 * @property dao of Retrofit library to download weather forecast data
 */
class CurrentWeatherLocalDataSourceImpl(private val dao: CurrentWeatherDAO) : CurrentWeatherLocalDataSource {

    @InternalSerializationApi
    override suspend fun loadWeather(city:String): Response<CurrentWeatherEntity> {
        val entry = dao.getCityForecast(city) ?: throw NoSuchDatabaseEntryException(city)
        Log.d("WeatherLocalDataSourceImpl", "${entry.city} city forecast loaded successfully")
        return Response.success(entry)
    }

    @InternalSerializationApi
    override suspend fun saveWeather(response: CurrentWeatherEntity) {
        dao.insertCityForecast(response)
        Log.d("WeatherLocalDataSourceImpl", "${response.city} weather saved successfully")
    }
}