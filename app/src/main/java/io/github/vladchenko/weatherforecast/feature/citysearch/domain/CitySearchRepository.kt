package io.github.vladchenko.weatherforecast.feature.citysearch.domain

import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CitySearch

/**
 * Retrieve cities names. Provides domain-layer data.
 */
interface CitySearchRepository {

    /**
     * Retrieve remote cities names for [token].
     */
    suspend fun loadCitiesNames(token: String): LoadResult<CitySearch>

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames()
}