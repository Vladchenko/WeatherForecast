package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.mapper.domaintouimapper

import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.CurrentWeather
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi

/**
 * Weather forecast domain to ui model mapper
 */
interface WeatherDomainToUiMapper {

    /** Convert domain [model] to ui model */
    fun toCurrentWeatherUi(model: CurrentWeather): CurrentWeatherUi
}