package com.example.weatherforecast.data.converter

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.util.WeatherForecastUtils.convertKelvinToCelsiusDegrees
import com.example.weatherforecast.data.util.WeatherForecastUtils.convertKelvinToFahrenheitDegrees
import com.example.weatherforecast.models.data.WeatherForecastResponse
import com.example.weatherforecast.models.domain.Coordinate
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import retrofit2.Response
import kotlin.math.roundToInt

/**
 * Convert data-layer model to domain-layer for weather forecast.
 */
class ForecastDataToDomainModelsConverter {

    /**
     * Convert server response to domain model, having a [temperatureType] and [city] provided.
     */
    fun convert(
        temperatureType: TemperatureType,
        city: String,
        response: Response<WeatherForecastResponse>
    ): WeatherForecastDomainModel {
        return WeatherForecastDomainModel(
            city,
            coordinate = Coordinate(
                latitude = response.body()?.coord?.lat ?: 0.0,
                longitude = response.body()?.coord?.lon ?: 0.0
            ),
            date = response.body()?.dt.toString(),
            temperature = defineTemperature(temperatureType, response),
            weatherType = response.body()?.weather?.get(0)?.description.orEmpty(),
            temperatureType = defineTemperatureSign(temperatureType),
            serverError = response.errorBody().toString().takeIf { it != "null" }.orEmpty()
        )
    }

    private fun defineTemperature(
        temperatureType: TemperatureType,
        response: Response<WeatherForecastResponse>
    ) = when (temperatureType) {
        TemperatureType.CELSIUS -> {
            convertKelvinToCelsiusDegrees(response.body()?.main?.temp ?: 0.0).roundToInt().toString()
        }

        TemperatureType.FAHRENHEIT -> {
            convertKelvinToFahrenheitDegrees(response.body()?.main?.temp ?: 0.0).roundToInt().toString()
        }

        TemperatureType.KELVIN -> {
            response.body()!!.main.temp.roundToInt().toString()
        }
    }

    private fun defineTemperatureSign(temperatureType: TemperatureType) =
        when (temperatureType) {
            TemperatureType.CELSIUS -> "℃"
            TemperatureType.FAHRENHEIT -> "℉"
            TemperatureType.KELVIN -> "°K"
        }
}