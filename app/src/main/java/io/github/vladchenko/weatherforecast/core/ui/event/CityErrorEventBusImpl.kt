package io.github.vladchenko.weatherforecast.core.ui.event

import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CityErrorEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Implementation of [CityErrorEventBus] using a [MutableSharedFlow].
 *
 * Provides thread-safe emission of [CityErrorEvent] instances to multiple UI collectors,
 * with replay of the latest event and an extra buffer slot to prevent drops.
 */
class CityErrorEventBusImpl : CityErrorEventBus {

    override val cityErrorEventFlow: SharedFlow<CityErrorEvent>
        get() = _cityErrorEventFlow

    private val _cityErrorEventFlow =
        MutableSharedFlow<CityErrorEvent>(extraBufferCapacity = 1)

    override fun send(event: CityErrorEvent) {
        _cityErrorEventFlow.tryEmit(event)
    }
}