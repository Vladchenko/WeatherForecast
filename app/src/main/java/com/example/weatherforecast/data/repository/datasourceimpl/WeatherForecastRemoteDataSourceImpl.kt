package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.models.data.WeatherForecastResponse
import retrofit2.Response

/**
 * Implementation of [WeatherForecastRemoteDataSource] that fetches weather forecast data from the remote API.
 *
 * @property apiService Service for weather forecast API operations
 * @property loggingService Service to log API responses
 * @property responseProcessor Processor to process API responses
 */
class WeatherForecastRemoteDataSourceImpl(
    private val apiService: WeatherForecastApiService,
    private val loggingService: LoggingService,
    private val responseProcessor: ResponseProcessor
) : WeatherForecastRemoteDataSource {

    override suspend fun loadForecastForCity(city: String): Response<WeatherForecastResponse> {
        val response = apiService.getWeatherForecast(city)
        loggingService.logApiResponse(TAG, "Weather forecast response for city = $city", response.body())
        return responseProcessor.processResponse(response)
    }

    override suspend fun loadForecastForLocation(latitude: Double, longitude: Double): Response<WeatherForecastResponse> {
        // Note: Location-based API is no longer supported in the current version
        throw UnsupportedOperationException("Location-based weather forecast is no longer supported")
    }

    companion object {
        private const val TAG = "WeatherForecastRemoteDataSourceImpl"
    }
}