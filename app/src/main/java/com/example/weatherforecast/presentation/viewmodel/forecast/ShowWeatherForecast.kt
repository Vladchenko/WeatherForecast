package com.example.weatherforecast.presentation.viewmodel.forecast

import com.example.weatherforecast.models.domain.WeatherForecastDomainModel

/**
 * TODO
 */
interface ShowWeatherForecast {
    fun showWeatherForecast(result: WeatherForecastDomainModel)
}