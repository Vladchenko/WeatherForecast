package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.models.data.CitiesNamesResponse
import kotlinx.serialization.InternalSerializationApi

/**
 * Data source for local cities names operations.
 */
interface CitiesNamesLocalDataSource {
    /**
     * Retrieve cities names for [token].
     */
    @InternalSerializationApi
    fun loadCitiesNames(token: String): List<CitiesNamesResponse>

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames()
}