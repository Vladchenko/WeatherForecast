package com.example.weatherforecast.models.domain

/**
 * Sealed class representing the result of a data loading operation.
 *
 * Used to handle different outcomes when fetching data from remote and local sources.
 *
 * @param T The type of the loaded domain model (e.g., [CurrentWeather])
 */
sealed class LoadResult<T> {

    /**
     * Successfully loaded fresh data from a remote source (e.g., API).
     *
     * @property data The domain model fetched from the remote source
     */
    data class Remote<T>(val data: T) : LoadResult<T>()

    /**
     * Fallback to cached data when remote load failed.
     *
     * @property data The cached domain model
     * @property remoteError Explanation of why remote fetch failed (e.g., "No internet")
     */
    data class Local<T>(val data: T, val remoteError: String) : LoadResult<T>()

    /**
     * Failed to load data from any source.
     *
     * @property exception The exception that caused the failure
     */
    data class Error<T>(val exception: Exception) : LoadResult<T>()
}