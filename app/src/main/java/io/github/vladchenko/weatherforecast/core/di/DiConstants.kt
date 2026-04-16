package io.github.vladchenko.weatherforecast.core.di

import io.github.vladchenko.weatherforecast.feature.citysearch.data.api.CitySearchApiService
import io.github.vladchenko.weatherforecast.feature.currentweather.data.api.CurrentWeatherApiService

object DiConstants {
    /**
     * Named binding for the Retrofit instance used with OpenWeatherMap API.
     *
     * Used to provide [CurrentWeatherApiService] and [CitySearchApiService].
     */
    const val WEATHER_RETROFIT_NAME = "WeatherRetrofit"
}