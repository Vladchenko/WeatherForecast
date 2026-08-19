package io.github.vladchenko.weatherforecast.core.geolocation

import kotlinx.coroutines.flow.SharedFlow

/**
 * Event bus for geolocation functionality.
 *
 * Provides a unified event-based interface for managing all geolocation-related operations,
 * including permission requests, raw location acquisition, reverse geocoding (city resolution),
 * and navigation triggers. Uses a single [SharedFlow] to consolidate all events, simplifying
 * UI-side consumption by replacing multiple individual flows with one cohesive stream.
 *
 * Implementations should emit events through [send] and expose them via [geoLocationEventsFlow]
 * for UI collectors.
 */
interface GeoLocationEventBus {

    /**
     * Unified event bus for the entire geolocation workflow.
     *
     * Consolidates all state changes, user interactions, and results into a single [SharedFlow].
     * Emits events of type [io.github.vladchenko.weatherforecast.core.geolocation.GeoLocationEvent], covering permission state transitions, raw location
     * acquisition, city resolution results, and navigation triggers. Replaces multiple individual flows to
     * simplify UI handling. The [extraBufferCapacity = 1] ensures events are not dropped during
     * ViewModel-to-UI state transitions.
     */
    val geoLocationEventsFlow: SharedFlow<GeoLocationEvent>

    /**
     * Publishes a single [GeoLocationEvent] into [geoLocationEventsFlow].
     *
     * Call this method from the ViewModel layer whenever a geolocation-related state change occurs
     * — permission updates, raw location fixes, city resolution results, or navigation triggers.
     * The event will be broadcast to all UI collectors via the underlying [SharedFlow].
     *
     * @param event the geolocation event to emit
     */
    fun send(event: GeoLocationEvent)
}