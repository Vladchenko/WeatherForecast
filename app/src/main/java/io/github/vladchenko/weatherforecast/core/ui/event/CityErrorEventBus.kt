package io.github.vladchenko.weatherforecast.core.ui.event

import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CityErrorEvent
import kotlinx.coroutines.flow.SharedFlow

/**
 * EventBus for broadcasting city-related error events to the UI layer.
 *
 * Provides a mechanism for components (e.g., ViewModels, interactors) to notify the UI
 * about errors that occur during city resolution or data fetching, such as missing or unknown cities.
 */
interface CityErrorEventBus {

    /**
     * SharedFlow that emits city-related error events.
     *
     * UI components should collect this flow to react to city errors by displaying
     * appropriate messages (e.g., "City not found"). Multiple collectors are supported,
     * and the flow replays the latest value to new subscribers.
     */
    val cityErrorEventFlow: SharedFlow<CityErrorEvent>

    /**
     * Sends a [CityErrorEvent] to all collectors of [cityErrorEventFlow].
     *
     * @param event The city error event to broadcast (e.g., [CityErrorEvent.CityNotFound]).
     */
    fun send(event: CityErrorEvent)
}