package io.github.vladchenko.weatherforecast.feature.recentcities.data.mapper

import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel
import io.github.vladchenko.weatherforecast.feature.recentcities.data.model.RecentCitiesEntity
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.model.RecentCities
import kotlinx.collections.immutable.toPersistentList

/**
 * Mapper class responsible for transforming [RecentCitiesEntity] database models
 * into domain-layer [RecentCities] objects and vice versa.
 *
 * This mapper bridges the data layer (Room entities) and the domain layer, ensuring
 * bidirectional conversion between persistent storage models and business logic models.
 *
 * Supports:
 * - Mapping lists of entities to immutable [RecentCities] collections
 * - Converting individual [CityDomainModel] instances to [RecentCitiesEntity]
 *   for persistence with current timestamp
 *
 * Maintains consistency in data format across layers and enables clean separation
 * between domain logic and data storage concerns.
 *
 * @see RecentCitiesEntity - source/sink entity from/to Room database
 * @see RecentCities - target domain model representing a collection of recent cities
 * @see CityDomainModel - simplified domain representation of a city
 */
class RecentCitiesMapper {

    /**
     * Maps a list of [RecentCitiesEntity] to a [RecentCities] domain object.
     *
     * Each entity is transformed into a [CityDomainModel] preserving:
     * - City name
     * - Country and state (if available)
     * - Geographic coordinates (lat/lon)
     *
     * The list is wrapped in a `PersistentList` for efficient updates and immutability.
     *
     * @param entities List of recently used cities from the database
     * @return A [RecentCities] instance containing the mapped cities
     */
    fun toDomain(entities: List<RecentCitiesEntity>): RecentCities {
        val cities = entities.map { entity ->
            CityDomainModel(
                name = entity.name,
                country = entity.country,
                state = entity.state,
                lat = entity.lat,
                lon = entity.lon
            )
        }.toPersistentList()

        return RecentCities(cities = cities)
    }

    /**
     * Maps a [CityDomainModel] to a [RecentCitiesEntity] for database storage.
     *
     * Converts domain representation of a city into a persistent entity with:
     * - Name, country, state, and coordinates copied directly
     * - Current system time as [lastUsed] timestamp
     *
     * Used when adding or updating a city in recent history.
     *
     * @param city The domain model to persist
     * @return A [RecentCitiesEntity] ready for database insertion
     */
    fun toEntity(city: CityDomainModel): RecentCitiesEntity {
        return RecentCitiesEntity(
            name = city.name,
            country = city.country,
            state = city.state.orEmpty(),
            lat = city.lat,
            lon = city.lon,
            lastUsed = System.currentTimeMillis()
        )
    }
}