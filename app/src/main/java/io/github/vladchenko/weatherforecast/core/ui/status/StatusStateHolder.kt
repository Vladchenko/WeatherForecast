package io.github.vladchenko.weatherforecast.core.ui.status

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
     * Emits the latest [StatusType] to all active collectors. UI components should observe
     * this flow to react to status changes in real-time.
     */
    val statusStateFlow: StateFlow<StatusType>

    /**
     * Updates the current status state.
     * Called by ViewModels or interactors to broadcast a new status to observing UI components.
     *
     * @param status the new [StatusType] to emit (e.g., [StatusType.Info], [StatusType.Warning], [StatusType.Error])
     */
    fun updateStatus(status: StatusType)
}