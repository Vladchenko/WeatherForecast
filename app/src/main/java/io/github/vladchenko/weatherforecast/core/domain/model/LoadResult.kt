package io.github.vladchenko.weatherforecast.core.domain.model

import io.github.vladchenko.weatherforecast.feature.currentweather.interactor.models.City
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Represents the result of a data loading operation, including source and error context.
 *
 * Designed for use in repositories that support fallback to local data when remote fetch fails.
 * This sealed interface allows exhaustive `when` expressions in UI and domain logic.
 *
 * Variants:
 * - [Remote]: Successfully loaded from a network API (fresh data).
 * - [Local]: Loaded from a local cache/database because the remote request failed (stale data).
 * - [Error]: Failed to load data from both remote and local sources.
 *
 * @see ForecastError
 */
sealed interface LoadResult<out T> {

    /**
     * Data successfully loaded from a remote source (e.g., network API).
     *
     * Indicates that the data is fresh and up-to-date.
     *
     * @param data the fetched domain model
     */
    data class Remote<out T>(val data: T) : LoadResult<T>

    /**
     * Data loaded from a local source (e.g., database) due to remote fetch failure.
     *
     * Used when the app falls back to cached data. The original remote error is preserved
     * in [remoteError] to inform the user or analytics about why local data is being shown.
     *
     * @param data the cached domain model
     * @param remoteError the reason why the remote request failed
     */
    data class Local<out T>(
        val data: T,
        val remoteError: ForecastError
    ) : LoadResult<T>

    /**
     * Failed to retrieve data from both remote and local sources.
     *
     * This state indicates a critical failure (e.g., no internet, corrupted cache, API error)
     * where no data is available to display.
     *
     * @param city The canonical city name resolved for the forecast (e.g., "London, GB").
     * @param requestedCity The original city input provided by the user (if any).
     *                      Useful for debugging or UI messaging to match the request with the response.
     * @param error The domain-level error describing the specific failure reason.
     */
    data class Error(
        val city: String? = null,
        val requestedCity: City? = null,
        val error: ForecastError
    ) : LoadResult<Nothing>
}

/**
 * Sealed interface representing domain-specific errors that can occur during data fetching.
 *
 * This hierarchy ensures exhaustive handling in UI and domain logic, improving robustness
 * and allowing meaningful error messages for the user.
 */
sealed interface ForecastError {
    /**
     * API key is invalid or missing.
     *
     * @param message error description from server
     */
    data class ApiKeyInvalid(val message: String) : ForecastError

    /**
     * Requested city was not found in the weather service.
     *
     * @param city name of the city that was not found
     * @param message detailed error message from API
     */
    data class CityNotFound(val city: String, val message: String) : ForecastError

    /**
     * Local cached data is corrupted or cannot be parsed.
     *
     * @param message description of the corruption or parsing issue
     */
    data class LocalDataCorrupted(val message: String) : ForecastError

    /**
     * Network-related error occurred (e.g., timeout, connection lost, SSL handshake failure).
     *
     * @param cause The underlying [Throwable] that caused the network error (e.g., [ConnectException]).
     * @param type The specific type of network error (nullable).
     */
    data class NetworkError(
        val cause: Throwable,
        val type: Type? = null
    ) : ForecastError {
        /**
         * Represents the type of network error.
         */
        enum class Type {
            /** No network connectivity available */
            NoInternet,

            /** Request took too long to complete */
            Timeout,

            /** Connection was refused or reset */
            ConnectionFailed,

            /** SSL/TLS handshake failed */
            SecurityError,

            /** Generic network issue not covered above */
            Other
        }

        companion object {
            /**
             * Factory method to create a [NetworkError] from a generic [Throwable].
             *
             * Analyzes the exception type to determine the specific [Type] of network error.
             *
             * @param cause The underlying throwable to wrap and classify.
             * @return A new [NetworkError] with the appropriate [Type].
             */
            fun fromThrowable(cause: Throwable): NetworkError {
                val type = when (cause) {
                    is ConnectException -> Type.ConnectionFailed
                    is UnknownHostException -> Type.NoInternet
                    is SocketTimeoutException -> Type.Timeout
                    is SSLException -> Type.SecurityError
                    else -> Type.Other
                }
                return NetworkError(cause, type)
            }
        }
    }

    /**
     * No data is available from any source.
     *
     * This error is used when the API returns no data or an empty response.
     *
     * @param message description of the data absence
     */
    data class NoDataAvailable(val message: String) : ForecastError

    /**
     * An error that does not fall into any of the predefined categories.
     *
     * Used as a fallback for unexpected exceptions not covered by specific error types.
     *
     * @param message description of the error
     * @param cause optional original exception for debugging (nullable)
     */
    data class UncategorizedError(
        val message: String,
        val cause: Throwable? = null
    ) : ForecastError
}