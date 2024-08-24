package com.example.weatherforecast.data.converter

import com.example.weatherforecast.models.data.WeatherForecastCityResponse
import com.example.weatherforecast.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.models.domain.CityDomainModel
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
                    it.name,
                    it.lat,
                    it.lon,
                    it.country,
                    it.state,
                    dataModel.errorBody()?.string() ?: error
                )
        }.orEmpty()
        return CitiesNamesDomainModel(cities, error)
    }
}