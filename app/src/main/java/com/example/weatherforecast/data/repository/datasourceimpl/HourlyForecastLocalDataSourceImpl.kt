package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.database.WeatherForecastDAO
import com.example.weatherforecast.data.repository.datasource.HourlyForecastLocalDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.models.data.City
import com.example.weatherforecast.models.data.HourlyForecastResponse

/**
 * [WeatherForecastRemoteDataSource] implementation.
 *
 * @property dao of Retrofit library to download weather forecast data
 * TODO Implement
 */
class HourlyForecastLocalDataSourceImpl(private val dao: WeatherForecastDAO) : HourlyForecastLocalDataSource {

    override suspend fun loadHourlyForecastData(city: String): HourlyForecastResponse {
//        Log.d("HourlyForecastLocalDataSourceImpl", "${entry.city} city forecast loaded successfully")
//        val entry = dao.getCityForecast(city) ?: throw NoSuchDatabaseEntryException(city)
//        return entry
        return HourlyForecastResponse(listOf(), City(0, "", "Berlin"))
    }

    override suspend fun saveHourlyForecastData(response: HourlyForecastResponse) {
//        dao.insertCityForecast(response)
        Log.d("HourlyForecastLocalDataSourceImpl", "${response.city} city forecast saved successfully")
    }
}