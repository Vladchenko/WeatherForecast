package io.github.vladchenko.weatherforecast.core.navigation

import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEvent
import kotlinx.coroutines.flow.SharedFlow

/**
 * An interface for broadcasting navigation events across the application.
 *
 * This event bus allows components to emit navigation requests and other components
 * to observe them via a shared flow.
 */
interface NavigationEventBus {
    /**
     * A shared flow that emits navigation events.
     *
     * Collectors can observe this flow to react to navigation requests triggered by
     * various parts of the application.
     */
    val navigationEventFlow: SharedFlow<NavigationEvent>

    /**
     * Sends a navigation event to the bus.
     *
     * @param event The [NavigationEvent] to be broadcast to all subscribers.
     */
    fun send(event: NavigationEvent)
}