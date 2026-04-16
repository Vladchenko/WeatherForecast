package io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasourceimpl

import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.core.utils.toDataError
import io.github.vladchenko.weatherforecast.data.util.ResponseProcessor
import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchResultDto
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.remote.CityApiService
import io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.remote.CitySearchRemoteDataSource
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Inject

/**
 * Implementation of [CitySearchRemoteDataSource] that fetches city data from the remote API.
 *
 * @property apiService Service for city-related API operations
 * @property loggingService Centralized service for structured logging
 * @property responseProcessor Utility class to process HTTP responses
 */
class CitySearchRemoteDataSourceImpl @Inject constructor(
    private val apiService: CityApiService,
    private val loggingService: LoggingService,
    private val responseProcessor: ResponseProcessor
) : CitySearchRemoteDataSource {

    @InternalSerializationApi
    override suspend fun loadCitiesNames(token: String): DataResult<List<CitySearchResultDto>> {
        return runCatching {
            val response = apiService.searchCities(token)
            loggingService.logDebugEvent(TAG, "Cities search response: ${response.body()}")
            return responseProcessor.processResponse(token, response)
        }.getOrElse { throwable ->
            loggingService.logError(TAG, "Unexpected error during API call for $token", throwable)
            DataResult.Error(token,throwable.toDataError())
        }
    }

    companion object {
        private const val TAG = "CitiesNamesDataSourceImpl"
    }
}