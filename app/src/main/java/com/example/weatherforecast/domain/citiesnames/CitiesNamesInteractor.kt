package com.example.weatherforecast.domain.citiesnames

import com.example.weatherforecast.models.domain.CitiesNames
import com.example.weatherforecast.models.domain.LoadResult

/**
 * Cities names interactor.
 *
 * @property citiesNamesRepository provides domain-layer data.
 */
class CitiesNamesInteractor(private val citiesNamesRepository: CitiesNamesRepository) {

    /**
     * Retrieve remote cities names matching [token].
     */
    suspend fun loadCitiesNames(token: String): LoadResult<CitiesNames> {
        return citiesNamesRepository.loadCitiesNames(token)
    }

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames() {
        citiesNamesRepository.deleteAllCitiesNames()
    }
}