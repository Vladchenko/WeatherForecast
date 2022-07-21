package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.models.WeatherForecastDomainModel
import com.example.weatherforecast.data.repository.datasource.WeatherForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource

/** TODO
 * [WeatherForecastRemoteDataSource] implementation.
 *
 * @property apiService Retrofit service to download weather data
 */
class WeatherForecastLocalDataSourceImpl(private val dao: WeatherForecastDAO) : WeatherForecastLocalDataSource {

    override suspend fun loadWeatherForecastData(city:String): WeatherForecastDomainModel {
        val model = dao.getCityForecast(city)
        Log.i("WeatherForecastLocalDataSourceImpl", "${model.city} city forecast loaded successfully")
        return model
    }

    override suspend fun saveWeatherForecastData(response: WeatherForecastDomainModel) {
        dao.insertCityForecast(response)
        Log.i("WeatherForecastLocalDataSourceImpl", "${response.city} city forecast saved successfully")
    }
}