package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.WeatherApiService
import com.example.weatherforecast.data.repository.datasource.HourlyWeatherRemoteDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.models.data.DataResult
import com.example.weatherforecast.models.data.network.HourlyWeatherDto
import kotlinx.serialization.InternalSerializationApi

/**
 * Implementation of [HourlyWeatherRemoteDataSource] that fetches hourly forecast data from the remote API.
 *
 * @property apiService Service for weather forecast API operations
 * @property loggingService Service to log API responses
 * @property responseProcessor Processor to process API responses
 */
class HourlyWeatherRemoteDataSourceImpl(
    private val apiService: WeatherApiService,
    private val loggingService: LoggingService,
    private val responseProcessor: ResponseProcessor
) : HourlyWeatherRemoteDataSource {

    @InternalSerializationApi
    override suspend fun loadHourlyWeatherForCity(city: String): DataResult<HourlyWeatherDto> {
        val response = apiService.getHourlyWeather(city)
        loggingService.logApiResponse(
            TAG,
            "Hourly forecast response for city = $city",
            response.body()
        )
        return responseProcessor.processResponse(response)
    }

    @InternalSerializationApi
    override suspend fun loadHourlyWeatherForLocation(
        latitude: Double,
        longitude: Double
    ): DataResult<HourlyWeatherDto> {
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