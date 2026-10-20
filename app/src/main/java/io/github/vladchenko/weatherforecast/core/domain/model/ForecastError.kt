package io.github.vladchenko.weatherforecast.core.domain.model

/**
 * Sealed interface representing domain-level errors for weather forecast operations.
 *
 * These errors are the result of mapping data-layer [io.github.vladchenko.weatherforecast.core.data.model.DataError]
 * instances through [io.github.vladchenko.weatherforecast.core.data.mapper.DataErrorToForecastErrorMapper].
 * They represent clean-domain concepts that are independent of the underlying data source implementation.
 *
 * ## Error Types
 * - [ApiKeyInvalid]: Authentication failed due to invalid or missing API key.
 * - [CityNotFound]: The requested city could not be found.
 * - [NetworkError]: Connectivity issues (no internet, timeout, connection failed, security error).
 * - [DataParsingError]: Response could not be parsed correctly.
 * - [ServerError]: Server returned an error response with a specific HTTP code.
 * - [AccessDenied]: Access denied by the server (e.g., IP blocked, security policy).
 * - [RateLimitExceeded]: Rate limit exceeded — too many requests.
 * - [LocalStorageError]: Local database or storage operation failed.
 * - [LocalDataCorrupted]: Cached/local data is corrupted or invalid.
 * - [NoDataAvailable]: No data is available (empty response or no cached data).
 * - [UncategorizedError]: An unexpected error that doesn't fit other categories.
 */
sealed interface ForecastError {

    /**
     * API key is invalid or missing.
     *
     * @param message description of the authentication failure.
     */
    data class ApiKeyInvalid(
        val message: String
    ) : ForecastError

    /**
     * The requested city could not be found.
     *
     * @param city the city name that was requested.
     * @param message description of the error.
     */
    data class CityNotFound(
        val city: String,
        val message: String
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
     * Response parsing error.
     *
     * @param message description of the parsing failure.
     */
    data class DataParsingError(
        val message: String
    ) : ForecastError

    /**
     * Server error with HTTP status code.
     *
     * @param code the HTTP status code.
     * @param message description of the server error.
     */
    data class ServerError(
        val code: Int,
        val message: String
    ) : ForecastError

    /**
     * Access denied by the server (e.g., IP blocked, security policy).
     *
     * @param message description of the access denial.
     */
    data class AccessDenied(
        val message: String
    ) : ForecastError

    /**
     * Rate limit exceeded — too many requests.
     *
     * @param message description of the rate limit error.
     */
    data class RateLimitExceeded(
        val message: String
    ) : ForecastError

    /**
     * Local storage (database) error.
     *
     * @param message description of the storage failure.
     */
    data class LocalStorageError(
        val message: String
    ) : ForecastError

    /**
     * Cached/local data is corrupted or invalid.
     *
     * @param message description of the corruption.
     */
    data class LocalDataCorrupted(
        val message: String
    ) : ForecastError

    /**
     * No data is available for the requested operation.
     *
     * @param message description of why no data is available.
     */
    data class NoDataAvailable(
        val message: String
    ) : ForecastError

    /**
     * An unexpected error that doesn't fit other categories.
     *
     * @param message description of the unexpected error.
     */
    data class UncategorizedError(
        val message: String
    ) : ForecastError
}
