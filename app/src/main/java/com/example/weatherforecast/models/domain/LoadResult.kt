package com.example.weatherforecast.models.domain

/**
 * Represents the result of a data loading operation, including source and error context.
 *
 * Designed for use in repositories that support fallback to local data when remote fetch fails.
 *
 * - [Remote]: Successfully loaded from network; represents fresh data.
 * - [Local]: Loaded from cache/database because remote request failed; includes the reason for failure.
 * - [Error]: Failed to load data both remotely and locally; indicates a critical failure.
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
     * to inform the user or analytics.
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
     * Indicates a serious issue (e.g., no internet, corrupted cache) where no data is available.
     *
     * @param city to provide weather forecast for
     * @param error the domain-level error describing the failure
     */
    data class Error(
        val city: String? = null,
        val error: ForecastError
    ) : LoadResult<Nothing>
}

/**
 * Sealed interface representing domain-specific errors that can occur during data fetching.
 *
 * Ensures exhaustive handling in UI and improves user experience by providing meaningful messages.
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
     * @param cause underlying exception (e.g., ConnectException, SocketTimeoutException)
     * @param type optional classification of network issue
     */
    data class NetworkError(
        val cause: Throwable,
        val type: Type? = null
    ) : ForecastError {

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
            fun fromThrowable(cause: Throwable): NetworkError {
                val type = when (cause) {
                    is java.net.ConnectException -> Type.ConnectionFailed
                    is java.net.UnknownHostException -> Type.NoInternet
                    is java.net.SocketTimeoutException -> Type.Timeout
                    is javax.net.ssl.SSLException -> Type.SecurityError
                    else -> Type.Other
                }
                return NetworkError(cause, type)
            }
        }
    }

    /**
     * No data is available from any source.
     *
     * @param message description of the data absence
     */
    data class NoDataAvailable(val message: String) : ForecastError

    /**
     * An error that does not fall into any of the predefined categories.
     *
     * Used as a fallback for exceptional cases not anticipated in normal operation flow.
     * Should be rare and typically indicates a need to expand error handling coverage.
     *
     * @param message description of the error
     * @param cause optional original exception for debugging
     */
    data class UncategorizedError(
        val message: String,
        val cause: Throwable? = null
    ) : ForecastError
}