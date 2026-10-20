package io.github.vladchenko.weatherforecast.core.di

import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.api.AISummaryApiService
import io.github.vladchenko.weatherforecast.feature.citysearch.data.api.CitySearchApiService
import io.github.vladchenko.weatherforecast.feature.currentweather.data.api.CurrentWeatherApiService
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Constants for configuring dependency injection (DI).
 *
 * Contains named bindings for Retrofit instances and API request timeouts
 * used in Dagger/Hilt modules.
 */
object DiConstants {
    /**
     * Named binding for the Retrofit instance used with OpenWeatherMap API.
     *
     * Used for providing [CurrentWeatherApiService] and [CitySearchApiService].
     */
    const val WEATHER_RETROFIT_NAME = "WeatherRetrofit"

    /**
     * Named binding for the Retrofit instance used with AI API.
     *
     * Used for providing [AISummaryApiService].
     */
    const val AI_SUMMARY_RETROFIT_NAME = "AiSummaryRetrofit"

    /**
     * Timeout in milliseconds for remote API requests.
     *
     * Used to prevent requests from hanging on slow or unavailable networks.
     * When exceeded, throws [TimeoutCancellationException],
     * which is handled in the data layer and converted to [DataResult.Error].
     */
    const val API_TIMEOUT_MS = 10000L

}