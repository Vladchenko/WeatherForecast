package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.models.data.WeatherForecastCityResponse
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import retrofit2.Response

/**
 * CitiesNamesDataSource implementation
 */
class CitiesNamesRemoteDataSourceImpl(private val apiService: WeatherForecastApiService) : CitiesNamesRemoteDataSource {

    override suspend fun getCityNames(token: String): Response<List<WeatherForecastCityResponse>> {
        val model = apiService.getCityNames(token)
        Log.d("CitiesNamesDataSourceImpl", model.body().toString())
        return model
    }
}