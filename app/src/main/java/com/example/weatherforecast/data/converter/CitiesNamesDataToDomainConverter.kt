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
        val converted = CitiesNamesDomainModel(
            if (dataModel.body().isNullOrEmpty()) {     //FIXME This might be shortened
                listOf(CityDomainModel(
                    name = "",
                    lat = 0.0,
                    lon = 0.0,
                    country = "",
                    state = "",
                    serverError = dataModel.errorBody()?.string()
                ))
            } else {
                dataModel.body()?.map {
                    CityDomainModel(
                        it.name,
                        it.lat,
                        it.lon,
                        it.country,
                        it.state,
                        serverError = ""
                    )
                }
            }.orEmpty()
        )
        return converted
    }
}