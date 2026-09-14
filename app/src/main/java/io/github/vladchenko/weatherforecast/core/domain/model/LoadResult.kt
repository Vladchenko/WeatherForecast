package io.github.vladchenko.weatherforecast.core.domain.model

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
 * Represents domain-level errors that can occur while loading weather data.
 *
 * This hierarchy contains only information meaningful to the domain and
 * presentation layers and does not depend on data-layer implementation details.
 */
sealed interface ForecastError {

    /**
     * API key is invalid or missing.
     */
    data class ApiKeyInvalid(
        val message: String
    ) : ForecastError

    /**
     * The requested city could not be found.
     */
    data class CityNotFound(
        val city: String,
        val message: String
    ) : ForecastError

    /**
     * A network operation failed.
     */
    data class NetworkError(
        val type: Type
    ) : ForecastError {

        enum class Type {
            NoInternet,
            Timeout,
            ConnectionFailed,
            SecurityError,
            Other
        }
    }

    /**
     * The server returned an error response.
     */
    data class ServerError(
        val code: Int,
        val message: String
    ) : ForecastError

    /**
     * The received data could not be parsed.
     */
    data class DataParsingError(
        val message: String
    ) : ForecastError

    /**
     * A local storage operation failed.
     */
    data class LocalStorageError(
        val message: String
    ) : ForecastError

    /**
     * Cached/local data is corrupted or invalid.
     */
    data class LocalDataCorrupted(
        val message: String
    ) : ForecastError

    /**
     * No weather data is available.
     */
    data class NoDataAvailable(
        val message: String
    ) : ForecastError

    /**
     * An unexpected error occurred.
     */
    data class UncategorizedError(
        val message: String
    ) : ForecastError
}