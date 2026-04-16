package io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasource

import io.github.vladchenko.weatherforecast.feature.recentcities.data.model.RecentCitiesEntity

/**
 * Data source interface for managing recently searched cities.
 *
 * Defines operations for retrieving and updating the list of recent cities, typically stored in a local database.
 * This interface abstracts the underlying persistence mechanism (e.g., Room) and provides a clean contract
 * for the repository layer to interact with user search history.
 *
 * Implementations are responsible for thread-safe data access, usually via coroutines.
 */
interface RecentCitiesDataSource {

    /**
     * Retrieves all recently searched cities from the data source.
     *
     * Results are expected to be ordered by last usage time in descending order (most recent first).
     *
     * @return A list of [RecentCitiesEntity], or an empty list if no recent cities are found.
     */
    suspend fun getRecentCities(): List<RecentCitiesEntity>

    /**
     * Adds a city to the recent cities list.
     *
     * If the city already exists, its entry should be updated (e.g., refresh the timestamp).
     * Uses the current system time as the last used value.
     *
     * @param entity The name of the city to add or update
     * @return The row ID of the inserted or updated record; -1 on failure
     */
    suspend fun addCityToRecents(entity: RecentCitiesEntity): Long
}