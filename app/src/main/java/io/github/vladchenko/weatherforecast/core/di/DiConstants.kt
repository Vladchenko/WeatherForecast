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

    /**
     * Timeout in milliseconds for remote API requests.
     *
     * Used to prevent hanging requests when the network is slow or unresponsive.
     * When exceeded, triggers [kotlinx.coroutines.TimeoutCancellationException]
     * which is handled by the data layer and converted to [DataResult.Error].
     */
    const val API_TIMEOUT_MS = 10000L

}