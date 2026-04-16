package io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasourceimpl

import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.core.utils.toDataError
import io.github.vladchenko.weatherforecast.feature.currentweather.data.api.CurrentWeatherApiService
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.feature.currentweather.data.model.CurrentWeatherDto
import io.github.vladchenko.weatherforecast.feature.currentweather.data.repository.datasource.CurrentWeatherRemoteDataSource
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
    private val apiService: CurrentWeatherApiService,
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