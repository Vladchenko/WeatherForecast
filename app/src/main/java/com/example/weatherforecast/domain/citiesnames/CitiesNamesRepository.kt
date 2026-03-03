package com.example.weatherforecast.domain.citiesnames

import com.example.weatherforecast.models.domain.CitiesNames
import com.example.weatherforecast.models.domain.LoadResult

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