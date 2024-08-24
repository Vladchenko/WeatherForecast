package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * [WeatherForecastRemoteDataSource] implementation.
 *
 * @property dao of Retrofit library to download weather forecast data
 */
class WeatherForecastLocalDataSourceImpl(private val dao: WeatherForecastDAO) : WeatherForecastLocalDataSource {

    override suspend fun loadForecastData(city:String): WeatherForecastDomainModel {
        val model = dao.getCityForecast(city) ?: throw NoSuchDatabaseEntryException(city)
        Log.d("WeatherForecastLocalDataSourceImpl", "${model.city} city forecast loaded successfully")
        return model
    }

    override suspend fun saveForecastData(response: WeatherForecastDomainModel) {
        dao.insertCityForecast(response)
        Log.d("WeatherForecastLocalDataSourceImpl", "${response.city} city forecast saved successfully")
    }
}