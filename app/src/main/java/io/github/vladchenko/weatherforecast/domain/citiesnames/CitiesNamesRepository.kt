package io.github.vladchenko.weatherforecast.domain.citiesnames

import io.github.vladchenko.weatherforecast.models.domain.CitiesNames
import io.github.vladchenko.weatherforecast.models.domain.LoadResult

/**
 * Retrieve cities names. Provides domain-layer data.
 */
interface CitiesNamesRepository {

    /**
     * Retrieve remote cities names for [token].
     */
    suspend fun loadCitiesNames(token: String): LoadResult<CitiesNames>

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames()
}