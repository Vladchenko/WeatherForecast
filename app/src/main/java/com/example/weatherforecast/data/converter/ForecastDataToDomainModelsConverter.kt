package com.example.weatherforecast.data.converter

import com.example.weatherforecast.data.models.data.WeatherForecastResponse
import com.example.weatherforecast.data.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.util.WeatherForecastUtils.getCelsiusFromKelvinTemperature
import com.example.weatherforecast.data.util.WeatherForecastUtils.getFahrenheitFromKelvinTemperature
import retrofit2.Response
import kotlin.math.roundToInt

/**
 * Convert data-layer model to domain-layer for weather forecast.
 */
class ForecastDataToDomainModelsConverter {

    /**
     * Convert server response to domain model
     */
    fun convert(
        temperatureType: TemperatureType,
        city: String,
        response: Response<WeatherForecastResponse>
    ): WeatherForecastDomainModel {
        val serverError = response.errorBody().toString()
        return WeatherForecastDomainModel(
            city,
            date = response.body()?.dt.toString(),
            temperature = defineTemperature(temperatureType, response),
            weatherType = response.body()?.weather?.get(0)?.description ?: "",
            temperatureType = defineTemperatureSign(temperatureType),
            serverError = if (serverError != "null") {
                serverError
            } else {
                ""
            }
        )
    }

    private fun defineTemperature(
        temperatureType: TemperatureType,
        response: Response<WeatherForecastResponse>
    ) = when (temperatureType) {
        TemperatureType.CELSIUS -> {
            getCelsiusFromKelvinTemperature(response.body()?.main?.temp ?: 0.0).roundToInt().toString()
        }
        TemperatureType.FAHRENHEIT -> {
            getFahrenheitFromKelvinTemperature(response.body()?.main?.temp ?: 0.0).roundToInt().toString()
        }
        TemperatureType.KELVIN -> {
            response.body()!!.main.temp.roundToInt().toString()
        }
    }

    private fun defineTemperatureSign(temperatureType: TemperatureType) =
        when (temperatureType) {
            TemperatureType.CELSIUS -> {
                "℃"
            }
            TemperatureType.FAHRENHEIT -> {
                "℉"
            }
            TemperatureType.KELVIN -> {
                "°K"
            }
        }
}