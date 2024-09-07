package com.example.weatherforecast.domain.citiesnames

import com.example.weatherforecast.models.domain.CitiesNamesDomainModel

/**
 * Cities names interactor.
 *
 * @property citiesNamesRepository provides data-layer data.
 */
class CitiesNamesInteractor(private val citiesNamesRepository: CitiesNamesRepository) {

    /**
     * Retrieve remote cities names matching [token].
     */
    suspend fun loadCitiesNames(token: String): CitiesNamesDomainModel {
        return citiesNamesRepository.loadCitiesNames(token)
    }

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames() {
        citiesNamesRepository.deleteAllCitiesNames()
    }
}