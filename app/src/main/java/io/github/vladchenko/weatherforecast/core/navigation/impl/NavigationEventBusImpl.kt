package io.github.vladchenko.weatherforecast.core.navigation.impl

import io.github.vladchenko.weatherforecast.core.navigation.api.NavigationEvent
import io.github.vladchenko.weatherforecast.core.navigation.api.NavigationEventBus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Implementation of [MutableSharedFlow] to broadcast navigation events.
 *
 * This class encapsulates the internal flow state and exposes a public [SharedFlow] for observers,
 * ensuring that only the bus itself can emit new events.
 */
class NavigationEventBusImpl : NavigationEventBus {

    override val navigationEventFlow: SharedFlow<NavigationEvent>
        get() = _navigationEventFlow

    private val _navigationEventFlow = MutableSharedFlow<NavigationEvent>(
        extraBufferCapacity = 1
    )

    override fun send(event: NavigationEvent) {
        _navigationEventFlow.tryEmit(event)
    }
}