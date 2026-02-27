package com.example.weatherforecast.data.repository.datasourceimpl

import com.example.weatherforecast.data.api.CityApiService
import com.example.weatherforecast.data.repository.datasource.CitiesNamesRemoteDataSource
import com.example.weatherforecast.data.util.LoggingService
import com.example.weatherforecast.models.data.network.CitiesSearchResultDto
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response
import javax.inject.Inject

/**
 * Implementation of [CitiesNamesRemoteDataSource] that fetches city data from the remote API.
 *
 * @property apiService Service for city-related API operations
 * @property loggingService Centralized service for structured logging
 */
class CitiesNamesRemoteDataSourceImpl @Inject constructor(
    private val apiService: CityApiService,
    private val loggingService: LoggingService
) : CitiesNamesRemoteDataSource {

    @InternalSerializationApi
    override suspend fun loadCitiesNames(token: String): Response<List<CitiesSearchResultDto>> {
        val response = apiService.searchCities(token)
        loggingService.logDebugEvent(TAG, "Cities search response: ${response.body()}")
        return response
    }

    companion object {
        private const val TAG = "CitiesNamesDataSourceImpl"
    }
}