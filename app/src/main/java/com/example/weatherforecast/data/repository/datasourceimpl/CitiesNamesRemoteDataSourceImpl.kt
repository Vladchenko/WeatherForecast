package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.CityApiService
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.data.repository.util.toDataError
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.data.util.ResponseProcessor
import com.example.weatherforecast.models.data.DataResult
import com.example.weatherforecast.models.data.network.CitiesSearchResultDto
import kotlinx.serialization.InternalSerializationApi
import javax.inject.Inject

/**
 * Implementation of [CitiesNamesRemoteDataSource] that fetches city data from the remote API.
 *
 * @property apiService Service for city-related API operations
 * @property loggingService Centralized service for structured logging
 * @property responseProcessor Utility class to process HTTP responses
 */
class CitiesNamesRemoteDataSourceImpl @Inject constructor(
    private val apiService: CityApiService,
    private val loggingService: LoggingService,
    private val responseProcessor: ResponseProcessor
) : CitiesNamesRemoteDataSource {

    @InternalSerializationApi
    override suspend fun loadCitiesNames(token: String): DataResult<List<CitiesSearchResultDto>> {
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