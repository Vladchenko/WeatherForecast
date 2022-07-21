package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.data.models.WeatherForecastDomainModel
import com.example.weatherforecast.data.models.WeatherForecastResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

/**
 * Local data source interface
 */
interface WeatherForecastLocalDataSource {
    /**
     * Download data, from a local storage, having [city] as a request parameter
     */
    suspend fun loadWeatherForecastData(city: String): WeatherForecastDomainModel

    /**
     * TODO
     * Save weather forecast data to local storage, having [city] as a request parameter
     */
    suspend fun saveWeatherForecastData(response: WeatherForecastDomainModel)
}