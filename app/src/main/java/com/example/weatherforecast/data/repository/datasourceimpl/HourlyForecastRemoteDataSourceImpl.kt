package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.repository.datasource.HourlyForecastRemoteDataSource
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.models.data.HourlyForecastResponse
import retrofit2.Response

/**
 * [WeatherForecastRemoteDataSource] implementation.
 *
 * @property apiService Retrofit service to download weather forecast data
 * @property loggingService Service to log API responses
 * @property responseProcessor Processor to process API responses
 */
class HourlyForecastRemoteDataSourceImpl(
    private val apiService: WeatherForecastApiService,
    private val loggingService: LoggingService,
    private val responseProcessor: ResponseProcessor
) : HourlyForecastRemoteDataSource {

    override suspend fun loadHourlyForecastForCity(city: String): Response<HourlyForecastResponse> {
        val response = apiService.loadHourlyForecastForCity(city)
        loggingService.logApiResponse(
            TAG,
            "hourly forecast response for city = $city",
            response.body()
        )
        return responseProcessor.processResponse(response)
    }

    override suspend fun loadHourlyForecastForLocation(
        latitude: Double,
        longitude: Double
    ): Response<HourlyForecastResponse> {
        val response = apiService.loadHourlyForecastForLocation(latitude, longitude)
        loggingService.logApiResponse(
            TAG,
            "hourly forecast response for latitude = $latitude, and longitude = $longitude",
            response.body()
        )
        return responseProcessor.processResponse(response)
    }

    companion object {
        private const val TAG ="HourlyForecastRemoteDataSourceImpl"
    }
}