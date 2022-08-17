package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * [WeatherForecastRemoteDataSource] implementation.
 *
 * @property dao Retrofit DAO to download weather data
 */
class WeatherForecastLocalDataSourceImpl(private val dao: WeatherForecastDAO) : WeatherForecastLocalDataSource {

    override suspend fun loadWeatherForecastData(city:String): WeatherForecastDomainModel {
        val model = dao.getCityForecast(city)
        Log.d("WeatherForecastLocalDataSourceImpl", "${model.city} city forecast loaded successfully")
        return model
    }

    override suspend fun saveWeatherForecastData(response: WeatherForecastDomainModel) {
        dao.insertCityForecast(response)
        Log.d("WeatherForecastLocalDataSourceImpl", "${response.city} city forecast saved successfully")
    }
}