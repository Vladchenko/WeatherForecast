package io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.datasource.remote

import io.github.vladchenko.weatherforecast.core.data.model.DataError
import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import io.github.vladchenko.weatherforecast.core.utils.toDataError
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.api.WeatherSummaryApiService
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryRequest
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.AISummaryResponse

/**
 * Implementation of [WeatherSummaryRemoteDataSource] for working with the remote API.
 * Uses [WeatherSummaryApiService] to send requests to the AI model.
 *
 * This implementation:
 * - Handles all technical exceptions (network, parsing, timeout) internally.
 * - Converts them into appropriate [DataError] types.
 * - Returns a [DataResult] to ensure predictable contract for upper layers.
 *
 * @param apiService service for working with the remote AI summary API.
 */
class WeatherSummaryRemoteDataSourceImpl(
    private val apiService: WeatherSummaryApiService,
) : WeatherSummaryRemoteDataSource {

    /**
     * Requests AI weather forecast analysis via the API.
     */
    override suspend fun fetchWeatherSummary(
        city: String,
        request: AISummaryRequest
    ): DataResult<AISummaryResponse> {
        return runCatching {
            val response = apiService.fetchWeatherSummary(request = request)
            if (response.isSuccessful) {
                response.body()?.let {
                    DataResult.Success(it)
                } ?: DataResult.Error(
                    city,
                    DataError.ResponseNoBodyError
                )
            } else {
                DataResult.Error(city, DataError.ServerError(response.code()))
            }
        }.getOrElse { throwable ->
            DataResult.Error(city, throwable.toDataError())
        }
    }
}