package io.github.vladchenko.weatherforecast.feature.citysearch.data.mapper

import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchEntity
import io.github.vladchenko.weatherforecast.feature.citysearch.data.model.CitySearchResultDto
import kotlinx.serialization.InternalSerializationApi

/**
 * Mapper class responsible for converting network [CitySearchResultDto] objects
 * into database [CitySearchEntity] objects.
 *
 * This mapper is used to transform a list of city search results received from the API
 * into entities suitable for local storage or UI presentation.
 *
 * Note: The use of [kotlinx.serialization.InternalSerializationApi] is required due to internal serialization
 * mechanisms in Kotlin Serialization; this class should be treated as stable despite the annotation.
 */
@InternalSerializationApi
class CitySearchDtoMapper {

    /**
     * Converts a list of [CitySearchResultDto] (network models) into a list of [CitySearchEntity] (database entities).
     * @return List of [CitySearchEntity] mapped from DTOs, containing essential city information
     *         such as name, country, state, latitude, and longitude.
     */
    fun toEntities(dtoList: List<CitySearchResultDto>): List<CitySearchEntity> =
        dtoList.map { cityDto ->
            CitySearchEntity(
                city = cityDto.name,
                country = cityDto.country,
                state = cityDto.state,
                latitude = cityDto.lat,
                longitude = cityDto.lon
            )
        }
}