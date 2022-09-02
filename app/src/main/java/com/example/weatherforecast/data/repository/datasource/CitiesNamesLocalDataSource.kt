package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Data source for cities names retrieval from database.
 */
interface CitiesNamesLocalDataSource {
    /**
     * Retrieve cities names for [token].
     */
    fun getCitiesNames(token: String): Flow<CityDomainModel>

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames()
}