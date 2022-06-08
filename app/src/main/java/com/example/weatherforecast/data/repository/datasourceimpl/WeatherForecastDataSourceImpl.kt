package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.models.WeatherForecastResponse
import com.example.weatherforecast.data.repository.datasource.WeatherForecastDataSource
import retrofit2.Response

/**
 * [WeatherForecastDataSource] implementation.
 *
 * @property apiService Retrofit service to download weather data
 */
class WeatherForecastDataSourceImpl(private val apiService: WeatherForecastApiService) : WeatherForecastDataSource {

    override suspend fun getWeatherForecastData(city:String): Response<WeatherForecastResponse> {
        return apiService.getWeatherForecastResponse(city)
    }
}