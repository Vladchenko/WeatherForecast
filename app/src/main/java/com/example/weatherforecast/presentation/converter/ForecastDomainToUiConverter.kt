package com.example.weatherforecast.presentation.converter

import com.example.weatherforecast.models.domain.WeatherForecast
import com.example.weatherforecast.models.presentation.WeatherForecastUi

/**
 * Weather forecast domain to ui model converter
 */
interface ForecastDomainToUiConverter {

    /**
     * Convert domain [model] to ui model, having [defaultErrorMessage] to define a default
     * message when date is incorrect and [getWeatherIconId] to get weather icon id
     */
    fun convert(model: WeatherForecast,
                defaultErrorMessage: String,
                getWeatherIconId: (String) -> Int): WeatherForecastUi
}