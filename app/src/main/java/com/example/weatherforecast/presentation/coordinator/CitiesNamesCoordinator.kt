package com.example.weatherforecast.presentation.coordinator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.weatherforecast.models.presentation.Message
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class CitiesNamesCoordinator(
    private val viewModel: CitiesNamesViewModel,
    private val statusRenderer: StatusRenderer
) {

    fun startObserving(scope: CoroutineScope, lifecycle: Lifecycle) {
        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMessageFlow(viewModel.messageSharedFlow) }
            }
        }
    }

    private suspend fun collectMessageFlow(flow: SharedFlow<Message>) {
        flow.collect { statusRenderer.updateFromMessage(it) }
    }
}