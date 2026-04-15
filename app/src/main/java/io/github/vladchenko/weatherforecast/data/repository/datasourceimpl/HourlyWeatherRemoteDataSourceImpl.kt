package io.github.vladchenko.weatherforecast.data.repository.datasourceimpl

import io.github.vladchenko.weatherforecast.core.data.models.DataResult
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.data.api.WeatherApiService
import io.github.vladchenko.weatherforecast.data.repository.datasource.HourlyWeatherRemoteDataSource
import io.github.vladchenko.weatherforecast.data.repository.util.toDataError
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.models.data.network.HourlyWeatherDto
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
        return runCatching {
            val response = apiService.loadHourlyWeather(city)
            loggingService.logApiResponse(
                TAG,
                "Hourly forecast response for city = $city",
                response.body()
            )
            return responseProcessor.processResponse(city, response)
        }.getOrElse { throwable ->
            loggingService.logError(TAG, "Unexpected error during API call for $city", throwable)
            DataResult.Error(city,throwable.toDataError())
        }
    }

    @InternalSerializationApi
    override suspend fun loadHourlyWeatherForLocation(
        city: String,
        latitude: Double,
        longitude: Double
    ): DataResult<HourlyWeatherDto> {
        return runCatching {
            val response = apiService.loadHourlyForecastByLocation(latitude, longitude)
            loggingService.logApiResponse(
                TAG,
                "Hourly forecast response for location (lat=$latitude, lon=$longitude)",
                response.body()
            )
            return responseProcessor.processResponse(city, response)
        }.getOrElse { throwable ->
            loggingService.logError(TAG, "Unexpected error during API call for $city", throwable)
            DataResult.Error(city,throwable.toDataError())
        }
    }

    companion object {
        private const val TAG = "HourlyForecastRemoteDataSourceImpl"
    }
}