package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.DataResult
import com.example.weatherforecast.models.data.network.CitiesSearchResultDto
import kotlinx.serialization.InternalSerializationApi

/**
 * Data source for cities names retrieval from network.
 */
interface CitiesNamesRemoteDataSource {
    /**
     * Retrieve cities names for [token].
     */
    @InternalSerializationApi
    suspend fun loadCitiesNames(token: String): DataResult<List<CitiesSearchResultDto>>
}