package com.example.weatherforecast.data.converter

import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response

/**
 * Converts data layer model to domain one for cities names request.
 */
class CitiesNamesModelConverter {

    /**
     * Convert [dataModel] and [error] to domain-layer model
     * @return domain-layer model
     */
    @InternalSerializationApi
    fun convert(
        dataModel: Response<List<WeatherForecastCityResponse>>,
        error: String
    ): CitiesNamesDomainModel {
        val cities = dataModel.body()?.map {
                CityDomainModel(
                    it.city,
                    it.latitude,
                    it.longitude,
                    it.country,
                    it.state,
                    dataModel.errorBody()?.string() ?: error
                )
        }.orEmpty().toPersistentList()
        return CitiesNamesDomainModel(cities, error)
    }
}