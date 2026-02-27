package com.example.weatherforecast.models.data

/**
 * Sealed interface representing the result of a data operation in the data layer.
 *
 * Encapsulates success and error cases for network or database operations without exposing domain-layer types.
 * Used internally by data sources, mappers, and repository implementations before conversion to [com.example.weatherforecast.models.domain.LoadResult].
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
     * @property error the description of what went wrong in data-layer terms
     */
    data class Error(val error: DataError) : DataResult<Nothing>
}

/**
 * Sealed interface representing recoverable errors that can occur during data operations in the data layer.
 *
 * These are low-level, technical errors related to networking, parsing, or local storage.
 * They must be mapped to domain-level [com.example.weatherforecast.models.domain.ForecastError] before being exposed to the UI.
 *
 * ## Error Types
 * - [NetworkError]: Connectivity issues (e.g., timeout, no internet).
 * - [ServerError]: HTTP 5xx or invalid server responses.
 * - [ResponseNoBodyError]: Successful HTTP response but empty body.
 * - [RequestFailError]: Client-side failure (e.g., 404 City Not Found).
 * - [ApiKeyInvalid]: Authentication failure due to invalid or missing API key.
 * - [DatabaseError]: Local database operation failed (e.g., query, insert).
 *
 * This sealed hierarchy ensures exhaustive handling within the data layer and supports future extensibility.
 */
sealed interface DataError {
    /**
     * Network-related error occurred (e.g., timeout, connection lost).
     *
     * @param cause underlying exception
     */
    data class NetworkError(val cause: Throwable) : DataError

    /**
     * Server returned an error response (e.g., 500 Internal Server Error).
     *
     * @param code HTTP status code
     * @param message server-provided message
     */
    data class ServerError(val code: Int, val message: String) : DataError

    /**
     * The API response was successful (2xx), but the body was null.
     */
    object ResponseNoBodyError : DataError

    /**
     * Request failed due to invalid parameters (e.g., city not found).
     *
     * @param requestBody the value used in the request (e.g., city name)
     * @param message server-provided error message
     */
    data class RequestFailError(val requestBody: String, val message: String) : DataError

    /**
     * API key is invalid or missing.
     *
     * @param message error description from server
     */
    data class ApiKeyInvalid(val message: String) : DataError

    /**
     * Local database operation failed (e.g., query, insert, delete).
     */
    object DatabaseError : DataError
}