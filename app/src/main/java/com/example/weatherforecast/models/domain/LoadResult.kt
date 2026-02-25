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
     * @param error the domain-level error describing the failure
     */
    data class Error(
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
     * Requested city was not found in the weather service.
     *
     * @param city name of the city that was not found
     * @param message detailed error message from API
     */
    data class CityNotFound(val city: String, val message: String) : ForecastError

    /**
     * Network-related error occurred (e.g., timeout, connection lost).
     *
     * @param cause underlying exception
     */
    data class NetworkError(val cause: Throwable) : ForecastError

    /**
     * Server returned an error response (e.g., 500 Internal Server Error).
     *
     * @param code HTTP status code
     * @param message server-provided message
     */
    data class ServerError(val code: Int, val message: String) : ForecastError

    /**
     * API key is invalid or missing.
     *
     * @param message error description from server
     */
    data class ApiKeyInvalid(val message: String) : ForecastError

    /**
     * No internet connection available.
     */
    object NoInternet : ForecastError

    /**
     * No data is available from any source.
     *
     * @param message description of the data absence
     */
    data class NoDataAvailable(val message: String) : ForecastError

    /**
     * Local cached data is corrupted or cannot be parsed.
     *
     * @param message description of the corruption or parsing issue
     */
    data class LocalDataCorrupted(val message: String) : ForecastError
}