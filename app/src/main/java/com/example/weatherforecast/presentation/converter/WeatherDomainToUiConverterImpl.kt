package com.example.weatherforecast.presentation.converter

import com.example.weatherforecast.models.domain.CurrentWeather
import com.example.weatherforecast.models.presentation.Coordinate
import com.example.weatherforecast.models.presentation.CurrentWeatherUi
import com.example.weatherforecast.presentation.PresentationConstants.UI_DATE_FORMAT
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Implementation of [WeatherDomainToUiConverter]
 */
class WeatherDomainToUiConverterImpl: WeatherDomainToUiConverter {

    private val formatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern(UI_DATE_FORMAT)
        .withLocale(Locale.getDefault())

    override fun convert(model: CurrentWeather,
                         defaultErrorMessage: String,
                         getWeatherIconId: (String) -> Int
    ): CurrentWeatherUi {
        val displayDate = try {
            val instant = Instant.ofEpochSecond(model.dateTime.toLong())
            val zone = ZoneOffset.ofTotalSeconds(model.timezone.toInt())
            val localDateTime = instant.atOffset(zone)
            formatter.format(localDateTime)
        } catch (e: Exception) {
            defaultErrorMessage
        }
        val iconId = getWeatherIconId(model.iconCode)
        return CurrentWeatherUi(
            city = model.city,
            coordinate = Coordinate(model.coordinate.latitude, model.coordinate.longitude),
            dateTime = displayDate,
            weatherIconId = iconId,
            temperature = model.temperature,
            weatherType = model.weatherType,
            temperatureType = model.temperatureType,
            serverError = model.serverError,
        )
    }
}