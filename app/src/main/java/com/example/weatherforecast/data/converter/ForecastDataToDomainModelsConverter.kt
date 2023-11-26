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

    fun convert(
        temperatureType: TemperatureType,
        city: String,
        response: Response<WeatherForecastResponse>
    ): WeatherForecastDomainModel {
        val responseBody = response.body()
        val main = responseBody?.main
        val weather = responseBody?.weather?.get(0)
        val temperature = main?.temp ?: 0.0

        return WeatherForecastDomainModel(
            city,
            coordinate = Coordinate(
                latitude = responseBody?.coord?.lat ?: 0.0,
                longitude = responseBody?.coord?.lon ?: 0.0
            ),
            date = responseBody?.dt.toString(),
            temperature = convertTemperature(temperatureType, temperature),
            weatherType = weather?.description.orEmpty(),
            temperatureType = defineTemperatureSign(temperatureType),
            serverError = response.errorBody()?.string().orEmpty()
        )
    }

    private fun convertTemperature(temperatureType: TemperatureType, temperature: Double) =
        when (temperatureType) {
            TemperatureType.CELSIUS -> "${convertKelvinToCelsiusDegrees(temperature).roundToInt()}"
            TemperatureType.FAHRENHEIT -> "${convertKelvinToFahrenheitDegrees(temperature).roundToInt()}"
            TemperatureType.KELVIN -> "${temperature.roundToInt()}"
        }

    private fun defineTemperatureSign(temperatureType: TemperatureType) =
        when (temperatureType) {
            TemperatureType.CELSIUS -> "℃"
            TemperatureType.FAHRENHEIT -> "℉"
            TemperatureType.KELVIN -> "°K"
        }
}