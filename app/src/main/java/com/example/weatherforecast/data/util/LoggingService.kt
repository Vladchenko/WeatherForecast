package com.example.weatherforecast.data.util

import android.util.Log
import com.example.weatherforecast.BuildConfig

/**
 * Service for centralized logging of application events and API responses.
 *
 * Provides a unified interface for logging that enables better control, consistency,
 * and potential extension points compared to direct use of [android.util.Log].
 *
 * ## Responsibilities
 * - Logs API responses with structured formatting for easier debugging.
 * - Records general application events with descriptive messages.
 * - Serves as a single point for future enhancements (e.g. log filtering, file output, crash reporting integration).
 *
 * ## Usage
 * - [logApiResponse]: Use in data sources to log Retrofit responses, including URL, parameters, and body.
 * - [logDebugEvent]: Use for significant internal events (e.g., cache updates, initialization steps).
 *
 * ## Benefits Over Direct Log.d Usage
 * - Enables testability: can be mocked in unit tests to verify logging behavior.
 * - Allows conditional logging (e.g., only in debug builds) by modifying the implementation once.
 * - Facilitates adding side effects like sending logs to analytics or writing to disk.
 * - Reduces code duplication and ensures consistent message format.
 *
 * ## Thread Safety
 * This class delegates to [Log], which is thread-safe. Safe to call from any thread, including coroutines on IO dispatcher.
 *
 * @see android.util.Log
 */
class LoggingService {

    private var isEnabled = BuildConfig.DEBUG

    /**
     * Logs an API response with a descriptive message and the full response body.
     *
     * Prints two sequential log lines:
     * 1. The provided message followed by "follows next"
     * 2. The string representation of the response object
     *
     * @param tag The log tag to identify the source (typically class name)
     * @param message A human-readable description of the request or context
     * @param response The response object to log; will be converted to string via [toString]
     */
    fun logApiResponse(tag: String, message: String, response: Any?) {
        if (isEnabled) {
            Log.d(tag, "$message follows next")
            Log.d(tag, response.toString())
        }
    }

    /**
     * Logs a general application event with a descriptive message at DEBUG level.
     *
     * Use this method for notable internal events such as initialization, state changes,
     * background tasks, or conditional logic decisions that are useful during development.
     *
     * @param tag The log tag to identify the source (e.g., class or subsystem name)
     * @param message A clear and concise description of the event
     */
    fun logDebugEvent(tag: String, message: String) {
        if (isEnabled) {
            Log.d(tag, message)
        }
    }

    /**
     * Logs an informational message indicating normal but noteworthy application activity.
     *
     * Use this method to track high-level application flow, such as startup phases,
     * major state transitions, or completion of key operations. Intended for events
     * that help understand app behavior without overwhelming the log output.
     *
     * @param tag The log tag to identify the source of the log
     * @param message A descriptive message about the event or state
     */
    fun logInfoEvent(tag: String, message: String) {
        if (isEnabled) {
            Log.i(tag, message)
        }
    }

    /**
     * Logs an error message without an associated exception.
     *
     * Use this method when a recoverable error condition occurs and there is no exception
     * to report, or when only a simple error message is needed.
     *
     * @param tag The log tag to identify the source of the error
     * @param message A descriptive message explaining the error condition
     */
    fun logError(tag: String, message: String) {
        if (isEnabled) {
            Log.e(tag, message)
        }
    }

    /**
     * Logs an error message along with exception details.
     *
     * Use this method when an exception has been caught and needs to be recorded
     * for debugging purposes. Includes both the provided message and the exception's message.
     *
     * @param tag The log tag to identify the source of the error
     * @param message A descriptive message about the context of the error
     * @param ex The exception that occurred, whose message will be included in the log
     */
    fun logError(tag: String, message: String, ex: Throwable) {
        if (isEnabled) {
            Log.e(tag, message + ": " + ex.message)
        }
    }
}