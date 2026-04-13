package io.github.vladchenko.weatherforecast.domain.recentcities

import io.github.vladchenko.weatherforecast.models.domain.CityDomainModel
import io.github.vladchenko.weatherforecast.models.domain.LoadResult
import io.github.vladchenko.weatherforecast.models.domain.RecentCities

/**
 * Interactor (use case) for managing recently searched cities at the domain layer.
 *
 * Encapsulates business logic related to loading and updating the list of recent cities.
 * Delegates actual data operations to [RecentCitiesRepository], ensuring separation between use cases and data handling.
 *
 * This class is responsible for:
 * - Retrieving the list of recent cities via [loadRecentCities]
 * - Adding a new city to the recents list via [addCityToRecents]
 *
 * It returns domain-specific results wrapped in [LoadResult], enabling consistent error/success handling
 * across the application.
 *
 * @property recentCitiesRepository repository that provides access to recent cities data (e.g., local database)
 */
class RecentCitiesInteractor(private val recentCitiesRepository: RecentCitiesRepository) {

    /**
     * Loads the list of recently searched cities.
     *
     * @return [LoadResult.Success] containing [RecentCities] if data is available,
     *         [LoadResult.Error] if an error occurs during retrieval
     */
    suspend fun loadRecentCities(): LoadResult<RecentCities> {
        return recentCitiesRepository.loadRecentCities()
    }

    /**
     * Adds a city to the list of recent cities.
     *
     * If the city already exists, its timestamp will be updated (refreshing recency).
     * Operation is persisted through the repository.
     *
     * @param city The name of the city to add or update
     */
    suspend fun addCityToRecents(city: CityDomainModel) {
        recentCitiesRepository.addCityToRecents(city)
    }
}