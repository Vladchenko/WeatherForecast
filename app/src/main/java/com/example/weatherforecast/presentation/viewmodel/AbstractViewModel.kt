package com.example.weatherforecast.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.R
import com.example.weatherforecast.connectivity.ConnectivityObserver
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.models.presentation.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * View model (MVVM component) with code common to inherent viewModels.
 *
 * @param connectivityObserver internet connectivity observer
 * @property coroutineDispatchers dispatchers for coroutines
 */
open class AbstractViewModel(
    connectivityObserver: ConnectivityObserver,
    private val coroutineDispatchers: CoroutineDispatchers
) : ViewModel() {

    /**
     * A [SharedFlow] that emits [Message] events to be shown in the UI.
     *
     * This flow is used to send one-off UI events such as success messages, warnings, or errors.
     * Uses `extraBufferCapacity = 1` to ensure the last message is delivered even if there are no active collectors
     * at the moment of emission (via [tryEmit]).
     */
    val messageSharedFlow: SharedFlow<Message>
        get() = _messageSharedFlow
    private val _messageSharedFlow = MutableSharedFlow<Message>(extraBufferCapacity = 1)

    /**
     * A mutable state that controls whether a progress bar should be shown in the UI.
     *
     * This state is typically observed in composables via `collectAsState()`.
     * Update this value directly (`showProgressBarState.value = true`) in subclasses
     * when a loading state needs to be reflected.
     */
    internal val showProgressBarState: MutableState<Boolean> = mutableStateOf(true)

    /**
     * A [StateFlow] that emits the current internet connectivity status.
     *
     * The value is `true` if the device has an active network connection,
     * and `false` otherwise. This state is derived from [connectivityObserver]
     * and shared via [SharingStarted.WhileSubscribed], meaning it stops collecting
     * when no observers are present (for more than 5 seconds), saving resources.
     *
     * It also emits an initial value of `true` while waiting for the first real check.
     */
    val internetConnectedStateFlow: StateFlow<Boolean> = connectivityObserver
        .isConnected
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            true
        )

    init {
        collectConnectivityEvents()
    }

    private fun collectConnectivityEvents() {
        viewModelScope.launch(coroutineDispatchers.io) {
            internetConnectedStateFlow.collect { isConnected ->
                if (isConnected) {
                    _messageSharedFlow.tryEmit(
                        Message.Success(
                            Message.Content.Resource(R.string.connected)
                        )
                    )
                } else {
                    _messageSharedFlow.tryEmit(
                        Message.Error(
                            Message.Content.Resource(R.string.disconnected)
                        )
                    )
                }
            }
        }
    }

    /**
     * Displays a success message in the UI.
     *
     * The message will be emitted through [messageSharedFlow] and can be collected
     * by the UI layer to show a toast, snackbar, or similar component.
     *
     * @param message the text of the message to display
     */
    fun showMessage(message: String) {
        _messageSharedFlow.tryEmit(Message.Success(Message.Content.Text(message)))
    }

    /**
     * Displays a success message using a string resource ID.
     *
     * Formats the string using provided arguments if necessary.
     *
     * @param resId resource ID of the string to display
     * @param args optional format arguments for the string resource
     */
    fun showMessage(@StringRes resId: Int, vararg args: Any) {
        _messageSharedFlow.tryEmit(Message.Success(Message.Content.Resource(resId, args)))
    }

    /**
     * Displays a warning message in the UI.
     *
     * @param message the text of the warning to display
     */
    fun showWarning(message: String) {
        _messageSharedFlow.tryEmit(Message.Warning(Message.Content.Text(message)))
    }

    /**
     * Displays an error message using a string resource ID.
     *
     * Formats the string using provided arguments if necessary.
     *
     * @param resId resource ID of the error message string
     * @param args optional format arguments for the string resource
     */
    fun showError(@StringRes resId: Int, vararg args: Any) {
        _messageSharedFlow.tryEmit(Message.Error(Message.Content.Resource(resId, args)))
    }

    /**
     * Displays an error message with raw text.
     *
     * @param message the text of the error to display
     */
    fun showError(message: String) {
        _messageSharedFlow.tryEmit(Message.Error(Message.Content.Text(message)))
    }
}