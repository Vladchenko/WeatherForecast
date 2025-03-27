package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.WeatherForecastApiService
import com.example.weatherforecast.data.repository.datasource.WeatherForecastRemoteDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.models.data.HourlyForecastResponse
import com.example.weatherforecast.models.data.WeatherForecastResponse
import retrofit2.Response

/**
 * [WeatherForecastRemoteDataSource] implementation.
 *
 * @property apiService Retrofit service to download weather forecast data
 * @property loggingService Service to log API responses
 * @property responseProcessor Processor to process API responses
 */
class WeatherForecastRemoteDataSourceImpl(
    private val apiService: WeatherForecastApiService,
    private val loggingService: LoggingService,
    private val responseProcessor: ResponseProcessor
) : WeatherForecastRemoteDataSource {

    override suspend fun loadForecastDataForCity(city: String): Response<WeatherForecastResponse> {
        val response = apiService.loadWeatherForecastForCity(city)
        loggingService.logApiResponse("WeatherForecastRemoteDataSourceImpl", "response for city = $city", response.body())
        return responseProcessor.processResponse(response)
    }

    override suspend fun loadForecastForLocation(latitude: Double, longitude: Double): Response<WeatherForecastResponse> {
        val response = apiService.loadWeatherForecastForLocation(latitude, longitude)
        loggingService.logApiResponse("WeatherForecastRemoteDataSourceImpl", "response for latitude = $latitude, and longitude = $longitude", response.body())
        return responseProcessor.processResponse(response)
    }

    override suspend fun loadHourlyForecastForCity(city: String): Response<HourlyForecastResponse> {
        val response = apiService.loadHourlyForecastForCity(city)
        loggingService.logApiResponse("WeatherForecastRemoteDataSourceImpl", "hourly forecast response for city = $city", response.body())
        return responseProcessor.processResponse(response)
    }

    override suspend fun loadHourlyForecastForLocation(latitude: Double, longitude: Double): Response<HourlyForecastResponse> {
        val response = apiService.loadHourlyForecastForLocation(latitude, longitude)
        loggingService.logApiResponse("WeatherForecastRemoteDataSourceImpl", "hourly forecast response for latitude = $latitude, and longitude = $longitude", response.body())
        return responseProcessor.processResponse(response)
    }
}