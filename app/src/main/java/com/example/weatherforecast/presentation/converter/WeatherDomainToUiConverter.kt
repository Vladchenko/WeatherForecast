package com.example.weatherforecast.presentation.converter

import com.example.weatherforecast.models.domain.CurrentWeather
import com.example.weatherforecast.models.presentation.CurrentWeatherUi

/**
 * Weather forecast domain to ui model converter
 */
interface WeatherDomainToUiConverter {

    /**
     * Convert domain [model] to ui model, having [defaultErrorMessage] to define a default
     * message when date is incorrect and [getWeatherIconId] to get weather icon id
     */
    fun convert(model: CurrentWeather,
                defaultErrorMessage: String,
                getWeatherIconId: (String) -> Int): CurrentWeatherUi
}