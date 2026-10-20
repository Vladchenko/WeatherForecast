package io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain

import io.github.vladchenko.weatherforecast.core.domain.model.HourlyWeather
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.Message
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather

/**
 * Standard implementation of [WeatherPromptBuilder] with configurable headers.
 *
 * Ensures domain-layer independence from Android for flexible localization.
 *
 * @param headers set of headers and labels for building prompts.
 */
class DefaultWeatherPromptBuilder(
    private val headers: SectionHeaders
) : WeatherPromptBuilder {

    /**
     * Set of headers and labels for building prompts.
     * Passed from presentation/DI layer (from R.string).
     *
     * @param systemPrompt system message content for the AI model.
     * @param currentWeatherTitle title for the current weather section.
     * @param hourlyForecastTitle title for the hourly forecast section.
     * @param dataUnavailable message displayed when weather data is unavailable.
     * @param noDetailedForecast message displayed when no detailed forecast data is available.
     * @param cityLabel label for the city field.
     * @param timeLabel label for the time field.
     * @param temperatureLabel label for the temperature field.
     * @param conditionsLabel label for the weather conditions field.
     * @param timezoneLabel label for the timezone field.
     * @param feelsLikeLabel label for the feels-like temperature field.
     * @param humidityLabel label for the humidity field.
     * @param windLabel label for the wind speed field.
     * @param forecastSeparator separator between forecast fields.
     */
    data class SectionHeaders(
        val systemPrompt: String,
        val currentWeatherTitle: String,
        val hourlyForecastTitle: String,
        val dataUnavailable: String,
        val noDetailedForecast: String,
        val cityLabel: String,
        val timeLabel: String,
        val temperatureLabel: String,
        val conditionsLabel: String,
        val timezoneLabel: String,
        val feelsLikeLabel: String,
        val humidityLabel: String,
        val windLabel: String,
        val forecastSeparator: String
    )

    override fun buildMessages(
        city: String,
        currentWeather: CurrentWeather?,
        hourlyWeather: HourlyWeather?,
        temperatureUnit: TemperatureUnit
    ): List<Message> {
        val userPrompt = buildUserPrompt(currentWeather, hourlyWeather)
        return listOf(
            Message(role = "system", content = headers.systemPrompt),
            Message(role = "user", content = userPrompt)
        )
    }

    private fun buildUserPrompt(
        currentWeather: CurrentWeather?,
        hourlyWeather: HourlyWeather?,
    ): String {
        val sb = StringBuilder()

        sb.appendLine(headers.currentWeatherTitle)
        if (currentWeather != null) {
            formatCurrentWeather(sb, currentWeather)
        } else {
            sb.appendLine(headers.dataUnavailable)
        }

        sb.appendLine()
        sb.appendLine(headers.hourlyForecastTitle)
        if (hourlyWeather != null) {
            formatHourlyWeather(sb, hourlyWeather)
        } else {
            sb.appendLine(headers.dataUnavailable)
        }

        return sb.toString()
    }

    private fun formatCurrentWeather(sb: StringBuilder, weather: CurrentWeather) {
        sb.appendLine("${headers.cityLabel}: ${weather.city}")
        sb.appendLine("${headers.timeLabel}: ${weather.dateTime}")
        sb.appendLine("${headers.temperatureLabel}: ${weather.temperature}")
        sb.appendLine("${headers.conditionsLabel}: ${weather.weatherType}")
        sb.appendLine("${headers.timezoneLabel}: ${weather.timezone}")
    }

    private fun formatHourlyWeather(sb: StringBuilder, hourly: HourlyWeather) {
        val forecasts = hourly.hourlyForecasts.take(24)
        if (forecasts.isEmpty()) {
            sb.appendLine(headers.noDetailedForecast)
        } else {
            forecasts.forEach { item ->
                sb.appendLine(
                    "${item.dateText}${headers.forecastSeparator}" +
                    "${headers.temperatureLabel}: ${item.temperature} | " +
                    "${headers.feelsLikeLabel}: ${item.feelsLike} | " +
                    "${headers.humidityLabel}: ${item.humidity}% | " +
                    "${headers.windLabel}: ${item.windSpeed} м/с | " +
                    "${item.weatherDescription}"
                )
            }
        }
    }
}
