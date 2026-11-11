package io.github.vladchenko.weatherforecast.core.di

import io.github.vladchenko.weatherforecast.core.data.model.DataResult
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Shared constants for dependency injection and HTTP configuration.
 *
 * Contains API request timeouts, HTTP header values, and base URLs
 * used across the application.
 */
object DiConstants {

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
