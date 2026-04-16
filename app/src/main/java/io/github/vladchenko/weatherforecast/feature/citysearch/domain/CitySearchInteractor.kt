package io.github.vladchenko.weatherforecast.feature.citysearch.domain

import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CitySearch
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.util.ValidationUtils

/**
 * Cities names interactor.
 *
 * @property citySearchRepository provides domain-layer data.
 */
class CitySearchInteractor(private val citySearchRepository: CitySearchRepository) {

    /**
     * Retrieve remote cities names matching [token].
     */
    suspend fun loadCitiesNames(token: String): LoadResult<CitySearch> {
        return ValidationUtils.validateCityToken(token)
            .map { citySearchRepository.loadCitiesNames(it) }
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
        citySearchRepository.deleteAllCitiesNames()
    }
}