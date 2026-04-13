package io.github.vladchenko.weatherforecast.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.vladchenko.weatherforecast.models.data.database.RecentCitiesEntity

/**
 * Data Access Object (DAO) for managing recently searched cities in the local database.
 *
 * This interface defines methods to interact with the `recentCitiesNames` table, allowing
 * insertion, retrieval, and deletion of city entries. Each entry is stored as a [RecentCitiesEntity]
 * and ordered by usage time for quick access to the most recent searches.
 *
 * The DAO uses Room persistence library annotations to map Kotlin functions to SQL operations.
 *
 * Usage:
 * - Insert or update a city using [insertOrUpdate]
 * - Retrieve all saved cities sorted by last use (newest first) via [loadRecentCitiesNames]
 * - Remove a specific city from history with [deleteCity]
 *
 * Thread Safety:
 * All methods are declared as `suspend` functions and should be called from a coroutine scope.
 * Room ensures thread-safe database access when used with coroutines.
 */
@Dao
interface RecentCitiesDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(city: RecentCitiesEntity): Long

    @Query("SELECT * FROM recentCitiesNames ORDER BY lastUsed DESC")
    suspend fun loadRecentCitiesNames(): List<RecentCitiesEntity>

    @Query("DELETE FROM recentCitiesNames WHERE name = :cityName")
    suspend fun deleteCity(cityName: String)

    @Query("DELETE FROM recentCitiesNames")
    suspend fun deleteAllCity()
}