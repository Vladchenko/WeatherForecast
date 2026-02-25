package com.example.weatherforecast.data.mapper

import com.example.weatherforecast.models.data.database.CitySearchEntity
import com.example.weatherforecast.models.data.network.CitiesSearchResultDto
import kotlinx.serialization.InternalSerializationApi

/**
 * Mapper class responsible for converting network [CitiesSearchResultDto] objects
 * into database [CitySearchEntity] objects.
 *
 * This mapper is used to transform a list of city search results received from the API
 * into entities suitable for local storage or UI presentation.
 *
 * Note: The use of [InternalSerializationApi] is required due to internal serialization
 * mechanisms in Kotlin Serialization; this class should be treated as stable despite the annotation.
 */
@InternalSerializationApi
class CitiesSearchDtoMapper {

    /**
     * Converts a list of [CitiesSearchResultDto] (network models) into a list of [CitySearchEntity] (database entities).
     * @return List of [CitySearchEntity] mapped from DTOs, containing essential city information
     *         such as name, country, state, latitude, and longitude.
     */
    fun toEntities(dtoList: List<CitiesSearchResultDto>): List<CitySearchEntity> =
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