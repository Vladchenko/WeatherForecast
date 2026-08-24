package io.github.vladchenko.weatherforecast.core.ui.status

import androidx.annotation.StringRes
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for managing and broadcasting application UI status messages.
 *
 * This interface defines the architecture for unidirectional status state management.
 * Implementations maintain the current application status and expose it as an observable
 * stream, allowing UI layers to react to loading, success, warning, or error events.
 *
 * Typical usage pattern:
 * 1. Business logic (ViewModels/Interactors) calls [updateStatus] to broadcast a new state.
 * 2. UI layer observes [statusStateFlow] to display snackbars, dialogs, or in-app notifications.
 *
 * @see StatusType
 * @see StatusStateHolderImpl
 */
interface StatusStateHolder {

    /**
     * Immutable public view of the current status state.
     *
     * Emits the latest [StatusData] to all active collectors. UI components should observe
     * this flow to react to status changes in real-time.
     */
    val statusStateFlow: StateFlow<StatusData>

    /**
     * Broadcasts an error status message using a string resource ID.
     *
     * @param stringId Resource ID of the error message string.
     * @param args Optional arguments to pass to [String.format] for string interpolation.
     */
    fun updateErrorStatus(@StringRes stringId: Int, vararg args: Any)

    /**
     * Broadcasts an error status message using a plain string.
     *
     * @param message The error message text.
     * @param args Optional arguments to pass to [String.format] for string interpolation.
     */
    fun updateErrorStatus(message: String, vararg args: Any)

    /**
     * Broadcasts an info status message using a string resource ID.
     *
     * @param stringId Resource ID of the info message string.
     * @param args Optional arguments to pass to [String.format] for string interpolation.
     */
    fun updateInfoStatus(@StringRes stringId: Int, vararg args: Any)

    /**
     * Broadcasts a warning status message using a string resource ID.
     *
     * @param stringId Resource ID of the warning message string.
     * @param args Optional arguments to pass to [String.format] for string interpolation.
     */
    fun updateWarningStatus(@StringRes stringId: Int, vararg args: Any)
}