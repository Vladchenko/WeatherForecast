package io.github.vladchenko.weatherforecast.core.ui.status

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of [StatusStateHolder] that manages UI status messages
 * using Kotlin Coroutines `StateFlow`.
 *
 * This class is responsible for holding the current application-wide status state
 * (e.g., loading, success, warning, error) and exposing it as an immutable `StateFlow`
 * for observation by UI components.
 *
 * The initial status is set to [StatusType.Info] with an empty message.
 *
 * @see StatusStateHolder
 */
class StatusStateHolderImpl() : StatusStateHolder {

    override val statusStateFlow: StateFlow<StatusType>
        get() = _statusStateFlow

    private val _statusStateFlow =
        MutableStateFlow<StatusType>(StatusType.Info(""))

    override fun updateStatus(status: StatusType) {
        _statusStateFlow.value = status
    }
}