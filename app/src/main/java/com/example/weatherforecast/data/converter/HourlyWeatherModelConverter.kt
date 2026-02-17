package com.example.weatherforecast.data.converter

import com.example.weatherforecast.data.util.TemperatureConversionUtils.convertKelvinToCelsiusDegrees
import com.example.weatherforecast.data.util.TemperatureConversionUtils.convertKelvinToFahrenheitDegrees
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.data.HourlyWeatherResponse
import com.example.weatherforecast.models.domain.HourlyItemDomainModel
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response
import kotlin.math.roundToInt

/**
 * Converts data layer model to domain one for hourly weather forecast
 */
class HourlyWeatherModelConverter {

    /**
     * Converts a Retrofit [Response] containing hourly weather data from the API into a domain model.
     *
     * @param temperatureType The unit type (Celsius, Fahrenheit, Kelvin) in which temperatures should be presented
     * @param city The name of the city for which the forecast is being converted; included in the resulting domain model
     * @param response The Retrofit [Response] object wrapping the [HourlyWeatherResponse], must not be null body
     * @return A fully populated [HourlyWeatherDomainModel] containing the city name and list of hourly forecasts
     * with temperatures converted according to [temperatureType]
     *
     * @throws IllegalStateException if the response body is null
     *
     * @note This method uses internal serialization APIs and is marked with [@InternalSerializationApi].
     * Ensure usage is safe and stable in the target environment.
     */
    @InternalSerializationApi
    fun convert(
        temperatureType: TemperatureType,
        city: String,
        response: Response<HourlyWeatherResponse>
    ): HourlyWeatherDomainModel {
        val body = response.body() ?: throw IllegalStateException("Response body is null")
        return HourlyWeatherDomainModel(
            city = city,
            hourlyForecasts = body.hourlyForecasts.map { item ->
                HourlyItemDomainModel(
                    timestamp = item.timestamp,
                    temperature = convertTemperature(item.main.temp, temperatureType),
                    feelsLike = convertTemperature(item.main.feelsLike, temperatureType),
                    humidity = item.main.humidity.toInt(),
                    windSpeed = item.wind.speed,
                    weatherDescription = item.weather.firstOrNull()?.description ?: "",
                    weatherIcon = item.weather.firstOrNull()?.icon ?: "",
                    dateText = item.dateText
                )
            }.toPersistentList()
        )
    }

    private fun convertTemperature(kelvin: Double, temperatureType: TemperatureType): String {
        return when (temperatureType) {
            TemperatureType.CELSIUS -> "${convertKelvinToCelsiusDegrees(kelvin).roundToInt()}"
            TemperatureType.FAHRENHEIT -> "${convertKelvinToFahrenheitDegrees(kelvin).roundToInt()}"
            TemperatureType.KELVIN -> "${kelvin.roundToInt()}"
        }
    }
} 