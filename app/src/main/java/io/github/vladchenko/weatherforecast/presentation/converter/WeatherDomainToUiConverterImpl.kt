package io.github.vladchenko.weatherforecast.presentation.converter

import io.github.vladchenko.weatherforecast.core.ui.constants.UiConstants.UI_DATE_FORMAT
import io.github.vladchenko.weatherforecast.feature.currentweather.domain.models.CurrentWeather
import io.github.vladchenko.weatherforecast.models.presentation.Coordinate
import io.github.vladchenko.weatherforecast.models.presentation.CurrentWeatherUi
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
                         toWeatherIconRes: (String) -> Int
    ): CurrentWeatherUi {
        val displayDate = try {
            val instant = Instant.ofEpochSecond(model.dateTime.toLong())
            val zone = ZoneOffset.ofTotalSeconds(model.timezone.toInt())
            val localDateTime = instant.atOffset(zone)
            formatter.format(localDateTime)
        } catch (_: Exception) {
            defaultErrorMessage
        }
        val iconId = toWeatherIconRes(model.iconCode)
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