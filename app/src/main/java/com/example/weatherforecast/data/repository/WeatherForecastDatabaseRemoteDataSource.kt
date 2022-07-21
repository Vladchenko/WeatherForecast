package com.example.weatherforecast.data.repository

import com.example.weatherforecast.data.models.WeatherForecastResponse
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import retrofit2.Response

class WeatherForecastDatabaseRemoteDataSource: WeatherForecastRemoteDataSource {
    override suspend fun getWeatherForecastData(city: String): Response<WeatherForecastResponse> {
        TODO("Not yet implemented")
    }
}