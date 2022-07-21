package com.example.weatherforecast.data.converter

import com.example.weatherforecast.data.models.WeatherForecastDomainModel
import com.example.weatherforecast.data.models.WeatherForecastResponse
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.util.WeatherForecastUtils.getCelsiusFromKelvinTemperature
import com.example.weatherforecast.data.util.WeatherForecastUtils.getFahrenheitFromKelvinTemperature
import retrofit2.Response
import kotlin.math.roundToInt

/**
 * TODO
 */
class DataToDomainModelsConverter {
    /**
     * TODO
     */
    fun convert(
        temperatureType: TemperatureType,
        city: String,
        response: Response<WeatherForecastResponse>
    ): WeatherForecastDomainModel {
        return WeatherForecastDomainModel(
            city,
            response.body()!!.dt.toString(),
            defineTemperature(temperatureType, response),
            response.body()!!.weather[0].description,
            defineTemperatureSign(temperatureType)
        )
    }

    private fun defineTemperature(
        temperatureType: TemperatureType,
        response: Response<WeatherForecastResponse>
    ) = when (temperatureType) {
        TemperatureType.CELSIUS -> {
            getCelsiusFromKelvinTemperature(response.body()!!.main.temp).roundToInt().toString()
        }
        TemperatureType.FAHRENHEIT -> {
            getFahrenheitFromKelvinTemperature(response.body()!!.main.temp).roundToInt().toString()
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