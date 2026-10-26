package io.github.vladchenko.weatherforecast.core.data.model

import io.github.vladchenko.weatherforecast.core.domain.model.ForecastError
import io.github.vladchenko.weatherforecast.core.domain.model.LoadResult

/**
 * Sealed interface representing the result of a data operation in the data layer.
 *
 * Encapsulates success and error cases for network or database operations without exposing domain-layer types.
 * Used internally by data sources, mappers, and repository implementations before conversion to [LoadResult].
 *
 * ## Purpose
 * - Ensures clean separation between layers by preventing data-layer components from depending on
 * domain models.
 * - Provides a standardized way to handle outcomes of remote and local data operations.
 * - Enables consistent error handling and fallback strategies in repositories.
 *
 * ## Implementation
 * - [Success] wraps successfully retrieved data (e.g., API response body).
 * - [Error] wraps a [DataError] instance that describes the nature of the failure.
 *
 * This sealed interface is part of the Clean Architecture design, where data-layer results are
 * mapped to domain-layer [LoadResult] at the boundary (repository).
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
 * They must be mapped to domain-level [ForecastError] before being exposed to the UI.
 */
sealed interface DataError {

    /**
     * API key is invalid or missing.
     */
    data object ApiKeyInvalid : DataError

    /**
     * The requested city could not be found by the remote service.
     */
    data class CityNotFound(
        val city: String
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
        val cause: Throwable? = null
    ) : DataError

    /**
     * The requested resource was not found (HTTP 404).
     */
    data object ResourceNotFound : DataError

    /**
     * Rate limit exceeded — too many requests (HTTP 429).
     */
    data object RateLimitExceeded : DataError

    /**
     * Access denied by the server (HTTP 403).
     */
    data object AccessDenied : DataError

    /**
     * The server returned an error response.
     */
    data class ServerError(
        val code: Int
    ) : DataError

    /**
     * A local database/storage operation failed.
     */
    data class DatabaseError(
        val cause: Throwable? = null
    ) : DataError

    /**
     * Network operation failed and no cached data is available.
     */
    data object NetworkAndCacheError : DataError

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