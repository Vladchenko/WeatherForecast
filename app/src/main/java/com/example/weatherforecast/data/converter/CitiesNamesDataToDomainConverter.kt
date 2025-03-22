package com.example.weatherforecast.data.converter

import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.models.domain.CityDomainModel
import kotlinx.collections.immutable.toPersistentList
import retrofit2.Response

/**
 * Convert data-layer model to domain-layer for cities names request.
 */
class CitiesNamesDataToDomainConverter {

    /**
     * Convert [dataModel] and [error] to domain-layer model
     * @return domain-layer model
     */
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