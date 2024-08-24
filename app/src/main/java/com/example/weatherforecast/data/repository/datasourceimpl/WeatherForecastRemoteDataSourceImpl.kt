package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.models.data.WeatherForecastResponse
import retrofit2.Response

/**
 * [WeatherForecastRemoteDataSource] implementation.
 *
 * @property apiService Retrofit service to download weather forecast data
 */
class WeatherForecastRemoteDataSourceImpl(private val apiService: WeatherForecastApiService) : WeatherForecastRemoteDataSource {

    override suspend fun loadForecastDataForCity(city: String): Response<WeatherForecastResponse> {
        Log.d("WeatherForecastRemoteDataSourceImpl", "response for city = $city, follows next")
        val model = apiService.loadWeatherForecastForCity(city)
        Log.d("WeatherForecastRemoteDataSourceImpl", model.body().toString())
        return model
    }

    override suspend fun loadForecastForLocation(latitude: Double, longitude: Double): Response<WeatherForecastResponse> {
        Log.d("WeatherForecastRemoteDataSourceImpl", "response for latitude = $latitude, and longitude = $longitude, follows next")
        val model = apiService.loadWeatherForecastForLocation(latitude, longitude)
        Log.d("WeatherForecastRemoteDataSourceImpl", model.body().toString())
        return model
    }
}