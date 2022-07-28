package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.models.data.WeatherForecastCityResponse
import com.example.weatherforecast.data.repository.datasource.CitiesNamesDataSource
import retrofit2.Response

/**
 * CitiesNamesDataSource implementation
 */
class CitiesNamesDataSourceImpl(private val apiService: WeatherForecastApiService) : CitiesNamesDataSource {

    override suspend fun getCityNamesForTyping(city: String): Response<List<WeatherForecastCityResponse>> {
        val model = apiService.getCityNamesForTyping(city)
        Log.i("CitiesNamesDataSourceImpl", model.body().toString())
        return model
    }
}