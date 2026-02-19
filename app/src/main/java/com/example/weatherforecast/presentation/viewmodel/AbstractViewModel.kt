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

    val messageFlow: SharedFlow<Message>
        get() = _messageFlow
    private val _messageFlow = MutableSharedFlow<Message>(extraBufferCapacity = 1)

    internal val showProgressBarState: MutableState<Boolean> = mutableStateOf(true)

    val internetConnectedState: StateFlow<Boolean> = connectivityObserver
        .isConnected
        .stateIn(     // Convert the Flow to a StateFlow (cold flow to a hot flow)
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            true
        )

    init {
        collectConnectivityEvents()
    }

    private fun collectConnectivityEvents() {
        viewModelScope.launch(coroutineDispatchers.io) {
            internetConnectedState.collect { isConnected ->
                if (isConnected) {
                    _messageFlow.tryEmit(
                        Message.Success(
                            Message.Content.Resource(R.string.connected)
                        )
                    )
                } else {
                    _messageFlow.tryEmit(
                        Message.Error(
                            Message.Content.Resource(R.string.disconnected)
                        )
                    )
                }
            }
        }
    }

    fun showMessage(message: String) {
        _messageFlow.tryEmit(Message.Success(Message.Content.Text(message)))
    }

    fun showMessage(@StringRes resId: Int, vararg args: Any) {
        _messageFlow.tryEmit(Message.Success(Message.Content.Resource(resId, args)))
    }

    fun showWarning(message: String) {
        _messageFlow.tryEmit(Message.Warning(Message.Content.Text(message)))
    }

    fun showError(@StringRes resId: Int, vararg args: Any) {
        _messageFlow.tryEmit(Message.Error(Message.Content.Resource(resId, args)))
    }

    fun showError(message: String) {
        _messageFlow.tryEmit(Message.Error(Message.Content.Text(message)))
    }
}