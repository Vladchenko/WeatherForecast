package io.github.vladchenko.weatherforecast.core.di

import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.data.api.WeatherSummaryApiService
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
     * Used for providing [WeatherSummaryApiService].
     */
    const val WEATHER_SUMMARY_RETROFIT_NAME = "WeatherSummaryRetrofit"

    /**
     * Timeout in milliseconds for remote API requests.
     *
     * Used to prevent requests from hanging on slow or unavailable networks.
     * When exceeded, throws [TimeoutCancellationException],
     * which is handled in the data layer and converted to [DataResult.Error].
     */
    const val API_TIMEOUT_MS = 10000L

    /**
     * HTTP header name for User-Agent.
     */
    const val HEADER_USER_AGENT = "User-Agent"

    /**
     * User-Agent value for HTTP requests.
     */
    const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/155.0.0.0 Safari/537.36"
}