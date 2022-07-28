package com.example.weatherforecast.data.converter

import com.example.weatherforecast.data.models.data.WeatherForecastCityResponse
import com.example.weatherforecast.data.models.domain.CitiesNamesDomainModel
import com.example.weatherforecast.data.models.domain.CityDomainModel
import retrofit2.Response

/**
 * Convert data-layer model to domain-layer for cities names request.
 */
class CitiesNamesDataToDomainConverter {

    /**
     * Convert [dataModel] to domain-layer model
     * @return domain-layer model
     */
    fun convert(dataModel: Response<List<WeatherForecastCityResponse>>): CitiesNamesDomainModel {
        return CitiesNamesDomainModel(
            dataModel.body()!!.map {
                CityDomainModel(
                    it.name,
                    it.lat,
                    it.lon,
                    it.country,
                    it.state
                )
            }
        )
    }
}