package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.repository.datasource.HourlyForecastRemoteDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.models.data.HourlyForecastResponse
import retrofit2.Response

/**
 * Implementation of [HourlyForecastRemoteDataSource] that fetches hourly forecast data from the remote API.
 *
 * @property apiService Service for weather forecast API operations
 * @property loggingService Service to log API responses
 * @property responseProcessor Processor to process API responses
 */
class HourlyForecastRemoteDataSourceImpl(
    private val apiService: WeatherForecastApiService,
    private val loggingService: LoggingService,
    private val responseProcessor: ResponseProcessor
) : HourlyForecastRemoteDataSource {

    override suspend fun loadHourlyForecastForCity(city: String): Response<HourlyForecastResponse> {
        val response = apiService.getHourlyForecast(city)
        loggingService.logApiResponse(
            TAG,
            "Hourly forecast response for city = $city",
            response.body()
        )
        return responseProcessor.processResponse(response)
    }

    override suspend fun loadHourlyForecastForLocation(
        latitude: Double,
        longitude: Double
    ): Response<HourlyForecastResponse> {
        val response = apiService.getHourlyForecastByLocation(latitude, longitude)
        loggingService.logApiResponse(
            TAG,
            "Hourly forecast response for location (lat=$latitude, lon=$longitude)",
            response.body()
        )
        return responseProcessor.processResponse(response)
    }

    companion object {
        private const val TAG = "HourlyForecastRemoteDataSourceImpl"
    }
}