package com.example.weatherforecast.data.mapper

import com.example.weatherforecast.models.data.database.CitySearchEntity
import com.example.weatherforecast.models.domain.CitiesNames
import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.collections.immutable.persistentListOf

/**
 * Mapper class responsible for converting [CitySearchEntity] database models
 * into domain-layer [CitiesNames] objects.
 *
 * This mapper is used to transform a list of locally stored city search results
 * into an immutable domain representation suitable for use in UI or business logic.
 */
class CitiesSearchEntityMapper {

    /**
     * Converts a list of [CitySearchEntity] into a [CitiesNames] object containing
     * an immutable list of [CityDomainModel] items.
     *
     * Currently, the `error` field is initialized as an empty string,
     * indicating no error — however, this may be updated in future implementations
     * to support error propagation from data sources.
     *
     * @return [CitiesNames] object with a persistent list of mapped cities and an empty error message.
     */
    fun toDomain(entities: List<CitySearchEntity>): CitiesNames {
        val cities = persistentListOf(*entities.map { toDomainItem(it) }.toTypedArray())
        return CitiesNames(cities = cities, error = "")
    }

    private fun toDomainItem(entity: CitySearchEntity): CityDomainModel =
        CityDomainModel(
            name = entity.city,
            lat = entity.latitude,
            lon = entity.longitude,
            country = entity.country,
            state = entity.state,
            serverError = null
        )
}