package io.github.vladchenko.weatherforecast.core.domain.model

import io.github.vladchenko.weatherforecast.core.data.mapper.DataErrorToForecastErrorMapper
import io.github.vladchenko.weatherforecast.core.data.model.DataError

/**
 * Sealed interface representing domain-level errors for weather forecast operations.
 *
 * These errors are the result of mapping data-layer [DataError] instances through
 * [DataErrorToForecastErrorMapper].
 * They represent clean-domain concepts that are independent of the underlying data source implementation.
 */
sealed interface ForecastError {

    /**
     * API key is invalid or missing.
     */
    data object ApiKeyInvalid : ForecastError

    /**
     * The requested city could not be found.
     *
     * @param city the city name that was requested.
     */
    data class CityNotFound(
        val city: String
    ) : ForecastError

    /**
     * Network connectivity error.
     *
     * @param type the type of network error.
     */
    data class NetworkError(
        val type: Type
    ) : ForecastError {

        /**
         * Types of network errors.
         */
        sealed class Type {
            /**
             * No internet connection available.
             */
            object NoInternet : Type()

            /**
             * Host cannot be resolved (DNS failure, host unavailable).
             */
            object HostUnreachable : Type()

            /**
             * Network request timed out.
             */
            object Timeout : Type()

            /**
             * Connection to server failed.
             */
            object ConnectionFailed : Type()

            /**
             * SSL/TLS security error.
             */
            object SecurityError : Type()

            /**
             * Other network error.
             */
            object Other : Type()
        }
    }

    /**
     * Network error combined with unavailable local cache.
     *
     * Indicates that the network request failed AND no cached data was available.
     */
    data object NetworkAndCacheError : ForecastError

    /**
     * Server error combined with unavailable local cache.
     *
     * Indicates that the server returned an error AND no cached data was available.
     *
     * @param code the HTTP status code.
     */
    data class ServerAndCacheError(
        val code: Int
    ) : ForecastError

    /**
     * Access denied combined with unavailable local cache.
     *
     * Indicates that access was denied AND no cached data was available.
     */
    data object AccessDeniedAndCacheError : ForecastError

    /**
     * Response parsing error.
     */
    data object DataParsingError : ForecastError

    /**
     * Server error with HTTP status code.
     *
     * @param code the HTTP status code.
     */
    data class ServerError(
        val code: Int
    ) : ForecastError

    /**
     * Access denied by the server (e.g., IP blocked, security policy).
     */
    data object AccessDenied : ForecastError

    /**
     * Rate limit exceeded — too many requests.
     */
    data object RateLimitExceeded : ForecastError

    /**
     * Local storage (database) error.
     */
    data object LocalStorageError : ForecastError

    /**
     * Cached/local data is corrupted or invalid.
     */
    data object LocalDataCorrupted : ForecastError

    /**
     * No data is available for the requested operation.
     */
    data object NoDataAvailable : ForecastError

    /**
     * The requested resource was not found (HTTP 404).
     */
    data object ResourceNotFound : ForecastError

    /**
     * An unexpected error that doesn't fit other categories.
     */
    data class UncategorizedError(
        val cause: Throwable?
    ) : ForecastError
}
