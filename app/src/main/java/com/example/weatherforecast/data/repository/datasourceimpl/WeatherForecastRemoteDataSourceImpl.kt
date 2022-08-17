package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.models.data.WeatherForecastResponse
import retrofit2.Response

/**
 * [WeatherForecastRemoteDataSource] implementation.
 *
 * @property apiService Retrofit service to download weather data
 */
class WeatherForecastRemoteDataSourceImpl(private val apiService: WeatherForecastApiService) :
    WeatherForecastRemoteDataSource {

    override suspend fun getWeatherForecastDataForCity(city: String): Response<WeatherForecastResponse> {
        val model = apiService.getWeatherForecastResponseForCity(city)
        Log.d("WeatherForecastRemoteDataSourceImpl", model.toString())
        return model
    }

    override suspend fun getWeatherForecastForLocation(latitude: Double, longitude: Double)
    : Response<WeatherForecastResponse> {
        val model = apiService.getWeatherForecastResponseForLocation(latitude, longitude)
        Log.d("WeatherForecastRemoteDataSourceImpl", model.toString())
        return model
    }
}