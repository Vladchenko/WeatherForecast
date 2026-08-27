package io.github.vladchenko.weatherforecast.feature.citysearch.domain

import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CitySearch
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.util.ValidationUtils

/**
 * Domain-level interactor responsible for searching city names.
 *
 * This interactor orchestrates the city search process by:
 * 1. Validating the search query token via [ValidationUtils]
 * 2. Delegating the actual data fetching to [CitySearchRepository]
 * 3. Handling validation errors and wrapping them in appropriate [LoadResult] or [ForecastError] types
 *
 * It serves as the bridge between the presentation layer and the data layer
 * for the city search feature, encapsulating the specific business logic
 * for token validation before fetching city data.
 *
 * @property citySearchRepository provides domain-layer data access for city search operations
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
                    city = token,
                    error = ForecastError.UncategorizedError(it.message ?: "Invalid query")
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