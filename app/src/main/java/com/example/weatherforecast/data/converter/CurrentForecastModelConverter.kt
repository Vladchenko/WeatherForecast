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
 * Converts data layer model to domain one for current time weather forecast
 */
class CurrentForecastModelConverter {

    /**
     * Convert [Response<WeatherForecastResponse>] to [WeatherForecastDomainModel].
     *
     * @param temperatureType Celsius / Kelvin / Fahrenheit
     * @param city name
     * @param response from remote server
     * @return domain data model
     */
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
                latitude = responseBody?.coordinate?.latitude ?: 0.0,
                longitude = responseBody?.coordinate?.longitude ?: 0.0
            ),
            dateTime = responseBody?.dateTime.toString(),
            temperature = convertTemperature(temperatureType, temperature),
            weatherType = weather?.description.orEmpty(),
            temperatureType = defineTemperatureSign(temperatureType),
            serverError = response.errorBody()?.string().orEmpty()
        )
    }

    private fun convertTemperature(temperatureType: TemperatureType, kelvin: Double) =
        when (temperatureType) {
            TemperatureType.CELSIUS -> "${convertKelvinToCelsiusDegrees(kelvin).roundToInt()}"
            TemperatureType.FAHRENHEIT -> "${convertKelvinToFahrenheitDegrees(kelvin).roundToInt()}"
            TemperatureType.KELVIN -> "${kelvin.roundToInt()}"
        }

    private fun defineTemperatureSign(temperatureType: TemperatureType) =
        when (temperatureType) {
            TemperatureType.CELSIUS -> "℃"
            TemperatureType.FAHRENHEIT -> "℉"
            TemperatureType.KELVIN -> "°K"
        }
}