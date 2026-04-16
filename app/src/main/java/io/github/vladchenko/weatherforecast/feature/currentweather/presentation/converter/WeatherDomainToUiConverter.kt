package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.converter

import io.github.vladchenko.weatherforecast.feature.currentweather.domain.models.CurrentWeather
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi

/**
 * Weather forecast domain to ui model converter
 */
interface WeatherDomainToUiConverter {

    /**
     * Convert domain [model] to ui model, having [defaultErrorMessage] to define a default
     * message when date is incorrect and [toWeatherIconRes] to get weather icon id
     */
    fun convert(model: CurrentWeather,
                defaultErrorMessage: String,
                toWeatherIconRes: (String) -> Int): CurrentWeatherUi
}