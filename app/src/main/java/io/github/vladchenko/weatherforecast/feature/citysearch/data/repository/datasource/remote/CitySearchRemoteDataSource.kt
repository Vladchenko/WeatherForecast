package io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource.remote

import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchResultDto

/**
 * Data source for cities names retrieval from network.
 */
interface CitySearchRemoteDataSource {
    /**
     * Retrieve cities names for [token].
     */
    suspend fun loadCitiesNames(token: String): DataResult<List<CitySearchResultDto>>
}