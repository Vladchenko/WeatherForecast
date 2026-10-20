package io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain

import io.github.vladchenko.weatherforecast.core.domain.model.HourlyWeather
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.models.Message
import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather

/**
 * Interface for building AI weather assistant messages based on weather data.
 *
 * Domain layer is independent of Android resources — strings are passed as parameters.
 */
interface WeatherPromptBuilder {

    /**
     * Builds a list of messages to send to the AI model.
     * Includes system prompt and user prompt with weather data.
     *
     * @param city city name.
     * @param currentWeather current weather (may be `null`).
     * @param hourlyWeather hourly forecast (may be `null`).
     * @param temperatureUnit temperature unit.
     * @return list of [Message] (system + user).
     */
    fun buildMessages(
        city: String,
        currentWeather: CurrentWeather?,
        hourlyWeather: HourlyWeather?,
        temperatureUnit: TemperatureUnit
    ): List<Message>
}
