package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Data source for local cities names operations.
 */
interface CitiesNamesLocalDataSource {
    /**
     * Retrieve cities names for [token].
     */
    fun loadCitiesNames(token: String): Flow<CityDomainModel>

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames()
}