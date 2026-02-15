package com.example.weatherforecast.data.converter

import com.example.weatherforecast.data.util.TemperatureConversionUtils.convertKelvinToCelsiusDegrees
import com.example.weatherforecast.data.util.TemperatureConversionUtils.convertKelvinToFahrenheitDegrees
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.data.HourlyForecastResponse
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.HourlyForecastItemDomainModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.InternalSerializationApi
import retrofit2.Response
import kotlin.math.roundToInt

/**
 * Converts data layer model to domain one for hourly weather forecast
 */
class HourlyForecastModelsConverter {
    @InternalSerializationApi
    fun convert(
        temperatureType: TemperatureType,
        city: String,
        response: Response<HourlyForecastResponse>
    ): HourlyForecastDomainModel {
        val body = response.body() ?: throw IllegalStateException("Response body is null")
        return HourlyForecastDomainModel(
            city = city,
            hourlyForecasts = body.hourlyForecasts.map { item ->
                HourlyForecastItemDomainModel(
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