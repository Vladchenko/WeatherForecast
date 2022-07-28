package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.models.data.WeatherForecastResponse
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
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
        Log.i("WeatherForecastRemoteDataSourceImpl", "$city city forecast downloaded successfully.")
        return model
    }

    override suspend fun getWeatherForecastForLocation(latitude: Double, longitude: Double)
    : Response<WeatherForecastResponse> {
        val model = apiService.getWeatherForecastResponseForLocation(latitude, longitude)
        Log.i(
            "WeatherForecastRemoteDataSourceImpl",
            "$latitude latitude, $longitude longitude forecast downloaded successfully."
        )
        return model
    }
}