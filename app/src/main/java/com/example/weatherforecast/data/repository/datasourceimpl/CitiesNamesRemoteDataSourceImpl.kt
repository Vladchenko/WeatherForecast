package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import retrofit2.Response

/**
 * [CitiesNamesRemoteDataSource] implementation.
 *
 * @param apiService Retrofit api to retrieve weather forecast data from network.
 */
class CitiesNamesRemoteDataSourceImpl(private val apiService: WeatherForecastApiService) : CitiesNamesRemoteDataSource {

    override suspend fun loadCitiesNames(token: String): Response<List<WeatherForecastCityResponse>> {
        val model = apiService.loadCitiesNames(token)
        Log.d("CitiesNamesDataSourceImpl", model.body().toString())
        return model
    }
}