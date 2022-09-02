package com.example.weatherforecast.domain.citiesnames

import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.coroutines.flow.Flow

/**
 * Cities names interactor.
 *
 * @property citiesNamesRepository provides data-layer data.
 */
class CitiesNamesInteractor(private val citiesNamesRepository: CitiesNamesRepository) {

    /**
     * Retrieve remote cities names matching [token].
     */
    suspend fun loadRemoteCitiesNames(token: String): CitiesNamesDomainModel {
        return citiesNamesRepository.loadRemoteCitiesNames(token)
    }

    /**
     * Retrieve local cities names matching [token].
     */
    fun loadLocalCitiesNames(token: String): Flow<CityDomainModel> {
        return citiesNamesRepository.loadLocalCitiesNames(token)
    }

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames() {
        citiesNamesRepository.deleteAllCitiesNames()
    }
}