package com.example.weatherforecast.domain.citiesnames

import com.example.weatherforecast.models.domain.CitiesNames
import com.example.weatherforecast.models.domain.ForecastError
import com.example.weatherforecast.models.domain.LoadResult
import com.example.weatherforecast.utils.ValidationUtils

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
        return ValidationUtils.validateCityToken(token)
            .map { citiesNamesRepository.loadCitiesNames(it) }
            .getOrElse {
                LoadResult.Error(
                    token,
                    ForecastError.UncategorizedError(it.message ?: "Invalid query")
                )
            }
    }

    /**
     * Delete all cities names.
     */
    suspend fun deleteAllCitiesNames() {
        citiesNamesRepository.deleteAllCitiesNames()
    }
}