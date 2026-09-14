package io.github.vladchenko.weatherforecast.core.data.model

/**
 * Sealed interface representing the result of a data operation in the data layer.
 *
 * Encapsulates success and error cases for network or database operations without exposing domain-layer types.
 * Used internally by data sources, mappers, and repository implementations before conversion to [io.github.vladchenko.weatherforecast.core.domain.model.LoadResult].
 *
 * ## Purpose
 * - Ensures clean separation between layers by preventing data-layer components from depending on domain models.
 * - Provides a standardized way to handle outcomes of remote and local data operations.
 * - Enables consistent error handling and fallback strategies in repositories.
 *
 * ## Implementation
 * - [Success] wraps successfully retrieved data (e.g., API response body).
 * - [Error] wraps a [DataError] instance that describes the nature of the failure.
 *
 * This sealed interface is part of the Clean Architecture design, where data-layer results are mapped to domain-layer [LoadResult] at the boundary (repository).
 */
sealed interface DataResult<out T> {
    /**
     * Represents a successful data operation with the resulting data.
     *
     * @property data the result of a successful operation; never null
     */
    data class Success<T>(val data: T) : DataResult<T>

    /**
     * Represents a failed data operation with an associated error.
     *
     * @property city that weather forecast is being provided for
     * @property error the description of what went wrong in data-layer terms
     */
    data class Error(val city: String, val error: DataError) : DataResult<Nothing>
}

/**
 * Sealed interface representing recoverable errors that can occur during data operations in the data layer.
 *
 * These are low-level, technical errors related to networking, parsing, or local storage.
 * They must be mapped to domain-level [io.github.vladchenko.weatherforecast.core.domain.model.ForecastError] before being exposed to the UI.
 *
 * ## Error Types
 * - [NetworkError]: Connectivity issues (e.g., timeout, no internet).
 * - [ServerError]: HTTP 5xx or invalid server responses.
 * - [ResponseNoBodyError]: Successful HTTP response but empty body.
 * - [CityNotFound]: Client-side failure (e.g., 404 City Not Found).
 * - [ApiKeyInvalid]: Authentication failure due to invalid or missing API key.
 * - [DatabaseError]: Local database operation failed (e.g., query, insert).
 *
 * This sealed hierarchy ensures exhaustive handling within the data layer and supports future extensibility.
 */

/**
 * Represents recoverable errors that can occur during data operations.
 *
 * Data-layer specific errors must be mapped to domain-layer [ForecastError]
 * before crossing the data/domain boundary.
 */
sealed interface DataError {

    /**
     * API key is invalid or missing.
     */
    data class ApiKeyInvalid(
        val message: String
    ) : DataError

    /**
     * The requested city could not be found by the remote service.
     */
    data class CityNotFound(
        val city: String,
        val message: String
    ) : DataError

    /**
     * A network operation failed.
     *
     * The original Throwable is kept in the data layer because it is
     * an implementation detail of the networking stack.
     */
    data class NetworkError(
        val cause: Throwable
    ) : DataError

    /**
     * The remote response could not be parsed.
     */
    data class ParsingError(
        val message: String,
        val cause: Throwable? = null
    ) : DataError

    /**
     * The server returned an error response.
     */
    data class ServerError(
        val code: Int,
        val message: String
    ) : DataError

    /**
     * A local database/storage operation failed.
     */
    data class DatabaseError(
        val message: String,
        val cause: Throwable? = null
    ) : DataError

    /**
     * The response was successful but contained no body.
     */
    data object ResponseNoBodyError : DataError

    /**
     * An unexpected error occurred.
     */
    data class UncategorizedError(
        val cause: Throwable
    ) : DataError
}