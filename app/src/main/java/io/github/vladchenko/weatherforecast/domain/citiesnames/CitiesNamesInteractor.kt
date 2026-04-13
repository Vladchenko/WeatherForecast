package io.github.vladchenko.weatherforecast.domain.citiesnames

import io.github.vladchenko.weatherforecast.models.domain.CitiesNames
import io.github.vladchenko.weatherforecast.models.domain.ForecastError
import io.github.vladchenko.weatherforecast.models.domain.LoadResult
import io.github.vladchenko.weatherforecast.utils.ValidationUtils

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