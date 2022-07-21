package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.models.WeatherForecastResponse
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import retrofit2.Response

/**
 * [WeatherForecastRemoteDataSource] implementation.
 *
 * @property apiService Retrofit service to download weather data
 */
class WeatherForecastRemoteDataSourceImpl(private val apiService: WeatherForecastApiService) : WeatherForecastRemoteDataSource {

    override suspend fun getWeatherForecastData(city:String): Response<WeatherForecastResponse> {
        val model = apiService.getWeatherForecastResponse(city)
        Log.i("WeatherForecastRemoteDataSourceImpl", "$city city forecast downloaded successfully.")
        return model
    }
}