package com.example.weatherforecast.data.repository.datasource

import com.example.weatherforecast.data.models.domain.CityDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Data source for cities names retrieval from database.
 */
interface CitiesNamesLocalDataSource {
    /**
     * Retrieve cities names for [token].
     */
    suspend fun getCitiesNames(token: String): Flow<List<CityDomainModel>>

    /**
     * Save [city] to database.
     */
    suspend fun saveCity(city: CityDomainModel)
}