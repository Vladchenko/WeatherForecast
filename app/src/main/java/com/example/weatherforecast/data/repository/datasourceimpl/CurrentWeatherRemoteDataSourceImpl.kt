package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.WeatherApiService
import com.example.weatherforecast.data.repository.datasource.CurrentWeatherRemoteDataSource
import com.example.weatherforecast.data.repository.util.toDataError
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.models.data.DataError
import com.example.weatherforecast.models.data.DataResult
import com.example.weatherforecast.models.data.network.CurrentWeatherDto
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * Implementation of [CurrentWeatherRemoteDataSource] that fetches weather forecast data from the remote API.
 *
 * This implementation:
 * - Handles all technical exceptions (network, parsing, timeout) internally.
 * - Converts them into appropriate [DataError] types.
 * - Returns a [DataResult] to ensure predictable contract for upper layers.
 *
 * @property apiService Service for weather forecast API operations
 * @property loggingService Service to log API responses and errors
 * @property responseProcessor Processor to process API responses into [DataResult]
 */
@InternalSerializationApi
class CurrentWeatherRemoteDataSourceImpl(
    private val apiService: WeatherApiService,
    private val loggingService: LoggingService,
    private val responseProcessor: ResponseProcessor
) : CurrentWeatherRemoteDataSource {

    override suspend fun loadWeatherForLocation(
        city: String,
        latitude: Double,
        longitude: Double
    ): DataResult<CurrentWeatherDto> {
        return runCatching {
            val response = apiService.loadCurrentWeatherForLocation(latitude, longitude)
            handleResponse(response, "lat=$latitude, lon=$longitude", city)
        }.getOrElse { throwable ->
            loggingService.logError(TAG, "Unexpected error during API call for $city", throwable)
            DataResult.Error(city, throwable.toDataError())
        }
    }

    private fun handleResponse(
        response: Response<CurrentWeatherDto>,
        context: String,
        city: String,
    ): DataResult<CurrentWeatherDto> {
        loggingService.logApiResponse(
            TAG,
            "Weather forecast response for $context",
            response.body()
        )
        return responseProcessor.processResponse(city, response)
    }

    companion object {
        private const val TAG = "CurrentWeatherRemoteDataSource"
    }
}