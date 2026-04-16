package io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource

import io.github.vladchenko.weatherforecast.core.data.models.DataResult
import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchResultDto
import kotlinx.serialization.InternalSerializationApi

/**
 * Data source for cities names retrieval from network.
 */
interface CitySearchRemoteDataSource {
    /**
     * Retrieve cities names for [token].
     */
    @InternalSerializationApi
    suspend fun loadCitiesNames(token: String): DataResult<List<CitySearchResultDto>>
}