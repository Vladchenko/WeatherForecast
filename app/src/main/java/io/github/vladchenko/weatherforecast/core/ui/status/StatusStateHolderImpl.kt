package io.github.vladchenko.weatherforecast.core.ui.status

import io.github.vladchenko.weatherforecast.R
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
 */
class StatusStateHolderImpl() : StatusStateHolder {

    override val statusStateFlow: StateFlow<StatusData>
        get() = _statusStateFlow

    override fun updateErrorStatus(stringId: Int, vararg args: Any) {
        _statusStateFlow.tryEmit(
            StatusData(
                message = TextType.ResId(stringId, args),
                messageColorAttr = R.attr.colorError
            )
        )
    }

    override fun updateErrorStatus(message: String) {
        _statusStateFlow.tryEmit(
            StatusData(
                message = TextType.Text(message),
                messageColorAttr = R.attr.colorError
            )
        )
    }

    override fun updateInfoStatus(stringId: Int, vararg args: Any) {
        _statusStateFlow.tryEmit(
            StatusData(
                message = TextType.ResId(stringId, args),
                messageColorAttr = R.attr.colorInfo
            )
        )
    }

    override fun updateWarningStatus(stringId: Int, vararg args: Any) {
        _statusStateFlow.tryEmit(
            StatusData(
                message = TextType.ResId(stringId, args),
                messageColorAttr = R.attr.colorWarning
            )
        )
    }

    private val _statusStateFlow =
        MutableStateFlow(
            StatusData(
                message = TextType.Empty,
                messageColorAttr = 0
            )
        )
}