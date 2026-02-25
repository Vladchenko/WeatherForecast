package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.WeatherApiService
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.models.data.network.CurrentWeatherDto
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * Implementation of [CurrentWeatherRemoteDataSource] that fetches weather forecast data from the remote API.
 *
 * @property apiService Service for weather forecast API operations
 * @property loggingService Service to log API responses
 * @property responseProcessor Processor to process API responses
 */
class CurrentWeatherRemoteDataSourceImpl(
    private val apiService: WeatherApiService,
    private val loggingService: LoggingService,
    private val responseProcessor: ResponseProcessor
) : CurrentWeatherRemoteDataSource {

    @InternalSerializationApi
    override suspend fun loadWeatherForCity(city: String): Response<CurrentWeatherDto> {
        val response = apiService.getCurrentWeatherForCity(city)
        loggingService.logApiResponse(
            TAG,
            "Weather forecast response for city = $city",
            response.body()
        )
        return responseProcessor.processResponse(response)
    }

    @InternalSerializationApi
    override suspend fun loadWeatherForLocation(
        latitude: Double,
        longitude: Double
    ): Response<CurrentWeatherDto> {
        val response = apiService.getCurrentWeatherForLocation(latitude, longitude)
        loggingService.logApiResponse(
            TAG,
            "Weather forecast response for location as lat=$latitude, lon=$longitude",
            response.body()
        )
        return responseProcessor.processResponse(response)
    }

    companion object {
        private const val TAG = "WeatherForecastRemoteDataSourceImpl"
    }
}