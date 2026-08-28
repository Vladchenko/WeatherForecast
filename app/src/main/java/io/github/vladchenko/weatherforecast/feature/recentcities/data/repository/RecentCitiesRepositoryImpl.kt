package io.github.vladchenko.weatherforecast.feature.recentcities.data.repository

import io.github.vladchenko.weatherforecast.core.domain.model.CityModel
import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.feature.recentcities.data.mapper.RecentCitiesMapper
import io.github.vladchenko.weatherforecast.feature.recentcities.data.model.RecentCitiesEntity
import io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasource.RecentCitiesDataSource
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.RecentCitiesRepository
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.model.RecentCities
import kotlinx.coroutines.withContext

/**
 * Concrete implementation of [RecentCitiesRepository] that provides access to recently searched cities
 * using a local data source via [RecentCitiesDataSource].
 *
 * This repository acts as a bridge between the domain layer and the data layer, applying mapping logic
 * and ensuring operations are executed on the appropriate thread using [CoroutineDispatchers].
 *
 * Key Responsibilities:
 * - Loads recent cities from the local database and maps them to domain models using [RecentCitiesMapper]
 * - Adds new cities or updates existing ones in the recents list
 * - Wraps results in [LoadResult] for consistent error/success handling across use cases
 * - Executes all data operations on the IO dispatcher to avoid blocking the main thread
 *
 * @property recentCitiesMapper transforms [RecentCitiesEntity] into domain-level [RecentCities]
 * @property coroutineDispatchers provides coroutine contexts for background execution
 * @property recentCitiesDataSource data source for persistent storage (e.g., Room database)
 */
class RecentCitiesRepositoryImpl(
    private val recentCitiesMapper: RecentCitiesMapper,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val recentCitiesDataSource: RecentCitiesDataSource
) : RecentCitiesRepository {

    override suspend fun loadRecentCities() =
        withContext(coroutineDispatchers.io) {
            LoadResult.Local(
                data = recentCitiesMapper.toDomain(
                    recentCitiesDataSource.getRecentCities()
                ),
                ForecastError.UncategorizedError("No error actually")
            )
        }

    override suspend fun addCityToRecents(city: CityModel) =
        withContext(coroutineDispatchers.io) {
            return@withContext recentCitiesDataSource.addCityToRecents(
                recentCitiesMapper.toEntity(city)
            )
        }

    override suspend fun deleteRecentCities() {
        withContext(coroutineDispatchers.io) {
            recentCitiesDataSource.deleteRecentCities()
        }
    }
}