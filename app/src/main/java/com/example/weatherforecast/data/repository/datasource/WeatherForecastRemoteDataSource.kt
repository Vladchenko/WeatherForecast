package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.WeatherForecastResponse
import retrofit2.Response

/**
 * Remote(network) data source interface.
 */
interface WeatherForecastRemoteDataSource {

    /**
     * Receives data, having [city] as a request parameter.
     */
    suspend fun loadWeatherForecastDataForCity(city: String): Response<WeatherForecastResponse>

    /**
     * Receives data, having [latitude] and [longitude] as a request parameter.
     */
    suspend fun loadWeatherForecastForLocation(latitude: Double, longitude: Double): Response<WeatherForecastResponse>
}