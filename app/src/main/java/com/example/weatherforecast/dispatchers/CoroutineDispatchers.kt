package com.example.weatherforecast.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Interface defining the coroutine dispatchers used throughout the application.
 *
 * Provides a dependency-injectable abstraction over [kotlinx.coroutines.Dispatchers],
 * allowing for better testability and separation of concerns.
 *
 * Implementations should provide appropriate dispatchers for:
 * - [io]: Disk and network operations (e.g., database, API calls)
 * - [main]: UI updates and interactions with LiveData/StateFlow
 * - [default]: CPU-intensive tasks (e.g., sorting, parsing large datasets)
 * - [unconfined]: Special cases where context switching is not required (use cautiously)
 */
interface CoroutineDispatchers {
    /**
     * Dispatcher for IO-bound tasks such as reading from/writing to disk,
     * making network requests, or database operations.
     */
    val io: CoroutineDispatcher

    /**
     * Dispatcher for interacting with the main thread (UI thread).
     * Used for updating UI elements and collecting flows in ViewModels.
     */
    val main: CoroutineDispatcher

    /**
     * Dispatcher for CPU-intensive computations.
     * Suitable for tasks like data processing, filtering, or JSON parsing.
     */
    val default: CoroutineDispatcher

    /**
     * Unconfined dispatcher that does not restrict execution to any specific thread.
     * Should be used carefully, primarily in testing or advanced coroutine control.
     */
    val unconfined: CoroutineDispatcher
}