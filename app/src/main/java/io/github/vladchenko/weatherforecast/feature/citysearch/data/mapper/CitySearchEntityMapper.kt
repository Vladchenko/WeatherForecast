package io.github.vladchenko.weatherforecast.feature.citysearch.data.mapper

import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchEntity
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CitySearch
import kotlinx.collections.immutable.persistentListOf

/**
 * Mapper class responsible for converting [CitySearchEntity] database models
 * into domain-layer [CitySearch] objects.
 *
 * This mapper is used to transform a list of locally stored city search results
 * into an immutable domain representation suitable for use in UI or business logic.
 */
class CitySearchEntityMapper {

    /**
     * Converts a list of [CitySearchEntity] into a [CitySearch] object containing
     * an immutable list of [CityDomainModel] items.
     *
     * Currently, the `error` field is initialized as an empty string,
     * indicating no error — however, this may be updated in future implementations
     * to support error propagation from data sources.
     *
     * @return [CitySearch] object with a persistent list of mapped cities and an empty error message.
     */
    fun toDomain(entities: List<CitySearchEntity>): CitySearch {
        val cities = persistentListOf(*entities.map { toDomainItem(it) }.toTypedArray())
        return CitySearch(cities = cities, error = "")
    }

    private fun toDomainItem(entity: CitySearchEntity): CityDomainModel =
        CityDomainModel(
            name = entity.city,
            lat = entity.latitude,
            lon = entity.longitude,
            country = entity.country,
            state = entity.state
        )
}