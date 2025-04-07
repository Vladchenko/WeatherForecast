package com.example.weatherforecast.data.repository.datasourceimpl

import android.util.Log
import com.example.weatherforecast.data.api.CityApiService
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import retrofit2.Response

/**
 * Implementation of [CitiesNamesRemoteDataSource] that fetches city data from the remote API.
 *
 * @param apiService Service for city-related API operations
 */
class CitiesNamesRemoteDataSourceImpl(private val apiService: CityApiService) : CitiesNamesRemoteDataSource {

    override suspend fun loadCitiesNames(token: String): Response<List<WeatherForecastCityResponse>> {
        val response = apiService.searchCities(token)
        Log.d(TAG, "Cities search response: ${response.body()}")
        return response
    }

    companion object {
        private const val TAG = "CitiesNamesDataSourceImpl"
    }
}