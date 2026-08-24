package io.github.vladchenko.weatherforecast.core.ui.status

import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
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
 * @property resourceManager provides access to application string resources
 */
class StatusStateHolderImpl(
    private val resourceManager: ResourceManager
) : StatusStateHolder {

    override val statusStateFlow: StateFlow<StatusData>
        get() = _statusStateFlow

    override fun updateErrorStatus(stringId: Int, vararg args: Any) {
        _statusStateFlow.tryEmit(
            StatusData(
            resourceManager.getString(stringId, *args),
            resourceManager.getThemeColorRes(R.attr.colorError)
            )
        )
    }

    override fun updateErrorStatus(message: String, vararg args: Any) {
        _statusStateFlow.tryEmit(
            StatusData(
                message,
                resourceManager.getThemeColorRes(R.attr.colorError)
            )
        )
    }

    override fun updateInfoStatus(stringId: Int, vararg args: Any) {
        _statusStateFlow.tryEmit(
            StatusData(
                resourceManager.getString(stringId, *args),
                resourceManager.getThemeColorRes(R.attr.colorInfo)
            )
        )
    }

    override fun updateWarningStatus(stringId: Int, vararg args: Any) {
        _statusStateFlow.tryEmit(
            StatusData(
                resourceManager.getString(stringId, *args),
                resourceManager.getThemeColorRes(R.attr.colorWarning)
            )
        )
    }

    private val _statusStateFlow =
        MutableStateFlow<StatusData>(StatusData("", 0))
}