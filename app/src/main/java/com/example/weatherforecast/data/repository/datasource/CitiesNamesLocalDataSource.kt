package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.database.CitySearchEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * Data source for local cities names operations.
 */
interface CitiesNamesLocalDataSource {
    /**
     * Retrieve cities names matching [token].
     */
    @InternalSerializationApi
    suspend fun loadCitiesNames(token: String): List<CitySearchEntity>

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames()
}