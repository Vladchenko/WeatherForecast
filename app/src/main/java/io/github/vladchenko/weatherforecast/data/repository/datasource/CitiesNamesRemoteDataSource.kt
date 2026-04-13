package io.github.vladchenko.weatherforecast.data.repository.datasource

import io.github.vladchenko.weatherforecast.models.data.DataResult
import io.github.vladchenko.weatherforecast.models.data.network.CitiesSearchResultDto
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