package io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasourceimpl

import io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService
import io.github.vladchenko.weatherforecast.feature.recentcities.data.model.RecentCitiesEntity
import io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasource.RecentCitiesDAO
import io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasource.RecentCitiesDataSource
import kotlinx.serialization.InternalSerializationApi

/**
 * Concrete implementation of [io.github.vladchenko.weatherforecast.feature.recentcities.data.repository.datasource.RecentCitiesDataSource] using Room database via [RecentCitiesDAO].
 *
 * This class provides persistent storage and retrieval of recently searched cities.
 * Each city is stored with its name and timestamp of last use, enabling recency-based sorting.
 *
 * Thread Safety:
 * All operations are `suspend` functions and safely executed on the caller's coroutine context.
 * The underlying Room DAO ensures thread-safe database access.
 *
 * Dependencies:
 * - [RecentCitiesDAO]: For executing database operations
 * - [io.github.vladchenko.weatherforecast.core.utils.logging.LoggingService]: For debugging and monitoring data access events
 */
class RecentCitiesDataSourceImpl(
    private val dao: RecentCitiesDAO,
    private val loggingService: LoggingService
): RecentCitiesDataSource {

    @InternalSerializationApi
    override suspend fun getRecentCities(): List<RecentCitiesEntity> {
        val entry = dao.loadRecentCitiesNames()
        loggingService.logDebugEvent(
            TAG,
            "Recent cities names loaded successfully. Count=${entry.size}."
        )
        return entry
    }

    @InternalSerializationApi
    override suspend fun addCityToRecents(entity: RecentCitiesEntity): Long {
        val rowId = dao.insertOrUpdate(entity)
        loggingService.logDebugEvent(TAG, "City '$entity' added or updated with rowId: $rowId")
        return rowId
    }

    override suspend fun deleteRecentCities() {
        dao.deleteAllCities()
    }


    companion object {
        private const val TAG = "RecentCitiesDataSource"
    }
}