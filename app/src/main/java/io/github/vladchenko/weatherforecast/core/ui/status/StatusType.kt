package io.github.vladchenko.weatherforecast.core.ui.status

/**
 * Sealed class representing the types of UI status messages.
 *
 * This hierarchy is used to broadcast application-wide status updates to the UI layer.
 * Each type corresponds to a specific visual treatment (e.g., color, icon) in the status bar,
 * snackbars, or dialogs.
 *
 * The three variants are:
 * - [Error]: Critical issues that prevent an operation from completing.
 * - [Info]: Neutral informational messages about successful operations or progress.
 * - [Warning]: Non-critical issues that require user attention but don't block the flow.
 *
 * @see StatusStateHolder
 */
sealed class StatusType {
    /**
     * Represents an error status.
     *
     * Used for critical issues such as network failures, API errors, corrupted data,
     * or any operation that failed and requires user awareness.
     *
     * @param message The error message to display to the user.
     */
    data class Error(val message: String) : StatusType()

    /**
     * Represents an informational status.
     *
     * Used for neutral messages such as successful data loading, progress updates,
     * or general notifications that don't require user action.
     *
     * @param message The informational message to display.
     */
    data class Info(val message: String) : StatusType()

    /**
     * Represents a warning status.
     *
     * Used for non-critical issues that warrant user attention but don't block operations.
     * Examples: showing stale cached data when the network request fails, temporary denials, etc.
     *
     * @param message The warning message to display.
     */
    data class Warning(val message: String) : StatusType()
}