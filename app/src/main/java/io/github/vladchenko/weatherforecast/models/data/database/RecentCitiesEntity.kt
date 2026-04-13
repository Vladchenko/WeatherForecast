package io.github.vladchenko.weatherforecast.models.data.database

import androidx.room.Entity

/**
 * Database entity representing a recently searched city.
 *
 * Uses a composite primary key (name, country, state) to uniquely identify a city,
 * ensuring correct handling of cities with identical names in different regions
 * (e.g., "Springfield" in different states or countries).
 *
 * This entity is used by [RecentCitiesDataSource] for local persistence and should
 * only be accessed through data source interfaces. Conversion to/from domain models
 * ([CityDomainModel]) is handled by [RecentCitiesMapper] in the repository layer.
 *
 * @property name The name of the city (e.g., "London")
 * @property country The country code or name (e.g., "GB", "United Kingdom")
 * @property state The administrative state or region (e.g., "California"), can be empty
 * @property lat Latitude coordinate for weather API requests
 * @property lon Longitude coordinate for weather API requests
 * @property lastUsed Timestamp (in milliseconds) when this city was last used,
 *                   used for ordering recent cities (most recent first)
 *
 * @see RecentCitiesDataSource
 * @see RecentCitiesMapper
 * @see CityDomainModel
 */
@Entity(
    tableName = "recentCitiesNames",
    primaryKeys = ["name", "country", "state"]
)
data class RecentCitiesEntity(
    val name: String,
    val country: String,
    val state: String,
    val lat: Double,
    val lon: Double,
    val lastUsed: Long
)