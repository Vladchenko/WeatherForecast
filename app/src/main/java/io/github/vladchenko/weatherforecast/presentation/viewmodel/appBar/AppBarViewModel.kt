package io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar

import androidx.annotation.AttrRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.network.NetworkStateHolder
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType
import io.github.vladchenko.weatherforecast.models.presentation.AppBarUiState
import io.github.vladchenko.weatherforecast.presentation.converter.appbar.AppBarStateMapper
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
 * @property resourceManager provides access to string resources (available for future extensibility)
 * @property appBarStateMapper converter for mapping forecast states (reserved for future use)
 * @property networkStateHolder manages network connectivity (connect or disconnect)
 */
@HiltViewModel
class AppBarViewModel @Inject constructor(
    private val stateHolder: StatusStateHolder,
    private val resourceManager: ResourceManager,
    private val appBarStateMapper: AppBarStateMapper,
    private val networkStateHolder: NetworkStateHolder
) : ViewModel() {

    /**
     * Read-only StateFlow emitting the current [AppBarUiState].
     *
     * Observed by the UI to update the toolbar's appearance (title, subtitle, colors).
     */
    val appBarUiStateFlow: StateFlow<AppBarUiState>
        get() = _appBarUiStateFlow.asStateFlow()

    private val _appBarUiStateFlow = MutableStateFlow(AppBarUiState())

    init {
        _appBarUiStateFlow.update {
            AppBarUiState(
                resourceManager.getString(R.string.app_name)
            )
        }
        viewModelScope.launch {
            stateHolder.statusStateFlow.collect { statusState ->
                when (statusState) {
                    is StatusType.Error -> {
                        updateSubtitle(statusState.message, R.attr.colorError)
                    }

                    is StatusType.Info -> {
                        updateSubtitle(statusState.message, R.attr.colorInfo)
                    }

                    is StatusType.Warning -> {
                        updateSubtitle(statusState.message, R.attr.colorWarning)
                    }
                }
            }
        }
        viewModelScope.launch {
            networkStateHolder.networkStateFlow.collect { state ->
                when (state) {
                    false -> {
                        updateSubtitle(
                            resourceManager.getString(R.string.network_disconnected),
                            R.attr.colorError
                        )
                    }

                    true -> {
                        updateSubtitle(
                            resourceManager.getString(R.string.network_connected),
                            R.attr.colorInfo
                        )
                    }
                }
            }
        }
    }

    private fun updateSubtitle(text: String, @AttrRes colorAttr: Int) {
        _appBarUiStateFlow.update {
            it.copy(
                subtitle = text,
                subtitleColorAttr = colorAttr
            ) // ← Сохраняем атрибут, не цвет
        }
    }
}