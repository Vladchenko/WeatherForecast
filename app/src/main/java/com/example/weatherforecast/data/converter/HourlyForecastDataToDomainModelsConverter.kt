package com.example.weatherforecast.data.converter

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.models.data.HourlyForecastResponse
import com.example.weatherforecast.models.domain.HourlyForecastDomainModel
import com.example.weatherforecast.models.domain.HourlyForecastItemDomainModel
import retrofit2.Response

class HourlyForecastDataToDomainModelsConverter {
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
            }
        )
    }

    private fun convertTemperature(kelvin: Double, temperatureType: TemperatureType): Double {
        return when (temperatureType) {
            TemperatureType.CELSIUS -> kelvin - 273.15
            TemperatureType.FAHRENHEIT -> (kelvin - 273.15) * 9 / 5 + 32
            TemperatureType.KELVIN -> kelvin
        }
    }
} 