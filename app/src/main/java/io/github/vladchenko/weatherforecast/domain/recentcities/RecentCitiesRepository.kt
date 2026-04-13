package io.github.vladchenko.weatherforecast.domain.recentcities

import io.github.vladchenko.weatherforecast.models.domain.CityDomainModel
import io.github.vladchenko.weatherforecast.models.domain.LoadResult
import io.github.vladchenko.weatherforecast.models.domain.RecentCities

/**
 * Repository interface defining the contract for accessing and managing recently searched cities
 * at the domain layer.
 *
 * This interface abstracts the data source (e.g., local database) and provides methods to load and update
 * the list of recent cities. It returns results wrapped in [LoadResult] to standardize success and error handling
 * across the application.
 *
 * Implementations are responsible for:
 * - Retrieving stored city names ordered by recency
 * - Persisting new or updated city entries with a timestamp
 */
interface RecentCitiesRepository {

    /**
     * Loads the list of recently searched cities from the data source.
     *
     * @return [LoadResult.Success] containing a [RecentCities] object if retrieval succeeds,
     *         [LoadResult.Error] if an exception occurs during loading
     */
    suspend fun loadRecentCities(): LoadResult<RecentCities>

    /**
     * Adds a city to the recent cities list or updates its timestamp if already present.
     *
     * This operation is persisted through the underlying data source (e.g., Room database).
     *
     * @param city The name of the city to add or update
     * @return The row ID of the inserted or updated record; -1 if the operation failed
     */
    suspend fun addCityToRecents(city: CityDomainModel): Long
}