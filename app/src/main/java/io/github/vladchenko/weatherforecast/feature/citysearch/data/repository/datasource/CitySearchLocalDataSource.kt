package io.github.vladchenko.weatherforecast.feature.citysearch.data.repository.datasource

import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchEntity
import kotlinx.serialization.InternalSerializationApi

/**
 * Data source for local cities names operations.
 */
interface CitySearchLocalDataSource {

    /**
     * Retrieve a list of city search entities from the local database that match the given token.
     *
     * Performs a case-insensitive partial search on the city name field.
     * Returns an empty list if no matches are found.
     *
     * @param token The search query to match against city names
     * @return List of [CitySearchEntity] instances matching the query
     */
    @InternalSerializationApi
    suspend fun loadCitiesNames(token: String): List<CitySearchEntity>

    /**
     * Save a list of city search entities into the local database.
     *
     * Inserts or replaces existing entries using the defined conflict strategy.
     * This method ensures that the latest city data is persisted for offline use.
     *
     * @param citiesNames The list of [CitySearchEntity] to be saved
     */
    suspend fun saveCitiesNames(citiesNames: List<CitySearchEntity>)

    /**
     * Delete all cities names from the local database.
     *
     * Removes every entry in the cities names table, effectively clearing the cache.
     * This operation is irreversible.
     */
    suspend fun deleteAllCitiesNames()
}