package io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar

import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.TextType
import io.github.vladchenko.weatherforecast.models.presentation.AppBarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the state of the application's App Bar (Toolbar).
 *
 * This ViewModel listens to global status updates from [StatusStateHolder] and
 * dynamically applies them to the App Bar's subtitle and text color. It ensures that
 * users receive immediate visual feedback for loading, informational, warning, or error states.
 *
 * State is emitted via [appBarUiStateFlow] and observed by the UI layer to update the Toolbar.
 *
 * @property stateHolder broadcasts application-wide status messages
 * @property networkStateHolder manages network connectivity (connect or disconnect)
 */
@HiltViewModel
class AppBarViewModel @Inject constructor(
    private val stateHolder: StatusStateHolder,
    private val networkStateHolder: NetworkStateHolder
) : ViewModel() {

    /**
     * Read-only StateFlow emitting the current [AppBarUiState].
     *
     * Observed by the UI to update the toolbar's appearance (title, subtitle, colors).
     */
    val appBarUiStateFlow: StateFlow<AppBarUiState>
        get() = _appBarUiStateFlow.asStateFlow()

    private val _appBarUiStateFlow =
        MutableStateFlow(AppBarUiState(titleResId = R.string.app_name))

    init {
        viewModelScope.launch {
            stateHolder.statusStateFlow.collect { statusState ->
                updateSubtitle(
                    subtitle = statusState.message,
                    colorAttr = statusState.messageColor
                )
            }
        }
        viewModelScope.launch {
            networkStateHolder.networkStateFlow.collect { state ->
                when (state) {
                    false -> {
                        stateHolder.updateErrorStatus(R.string.network_disconnected)
                    }

                    true -> {
                        stateHolder.updateInfoStatus(R.string.network_connected)
                    }
                }
            }
        }
    }

    /**
     * Update title string resource with [titleResId]
     */
    fun updateTitle(@StringRes titleResId: Int) {
        _appBarUiStateFlow.update {
            it.copy(titleResId = titleResId)
        }
    }

    private fun updateSubtitle(
        subtitle: TextType,
        @AttrRes colorAttr: Int
    ) {
        _appBarUiStateFlow.update {
            it.copy(
                subtitle = subtitle,
                subtitleColorAttr = colorAttr
            )
        }
    }
}