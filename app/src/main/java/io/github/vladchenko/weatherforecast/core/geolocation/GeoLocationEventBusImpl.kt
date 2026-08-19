package io.github.vladchenko.weatherforecast.core.geolocation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Default implementation of [GeoLocationEventBus] backed by a [MutableSharedFlow].
 *
 * Consolidates all geolocation events into a single shared flow with `extraBufferCapacity = 1`
 * to prevent event loss during ViewModel-to-UI transitions. Events emitted via [send]
 * are broadcast to all UI collectors through [geoLocationEventsFlow].
 */
class GeoLocationEventBusImpl : GeoLocationEventBus {
    
    override val geoLocationEventsFlow: SharedFlow<GeoLocationEvent>
        get() = _geoLocationEventsFlow

    private val _geoLocationEventsFlow =
        MutableSharedFlow<GeoLocationEvent>(extraBufferCapacity = 1, replay = 1)

    override fun send(event: GeoLocationEvent) {
        _geoLocationEventsFlow.tryEmit(event)
    }
}