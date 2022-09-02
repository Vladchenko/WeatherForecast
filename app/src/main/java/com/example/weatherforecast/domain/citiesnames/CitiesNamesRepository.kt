package com.example.weatherforecast.domain.citiesnames

import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Retrieve cities names. Provides domain-layer data.
 */
interface CitiesNamesRepository {

    /**
     * Retrieve remote cities names for [token].
     */
    suspend fun loadRemoteCitiesNames(token: String): CitiesNamesDomainModel

    /**
     * Retrieve local cities names for [token].
     */
    fun loadLocalCitiesNames(token: String): Flow<CityDomainModel>

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames()
}