package io.github.vladchenko.weatherforecast.feature.geolocation.domain

import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel

/**
 * Callback interface for handling geolocation-related user actions and navigation events.
 *
 * This interface is implemented by UI components (e.g., Fragments) to react to outcomes of the
 * geolocation workflow in a type-safe and maintainable way. Instead of multiple individual callback methods,
 * all events are delivered through a single [onEvent] method with a [GeoLocationCallbackEvent] sealed hierarchy,
 * enabling exhaustive handling via `when` expressions.
 *
 * Responsibilities include:
 * - Navigating to city selection screen
 * - Requesting location permissions from the system
 * - Handling permanent permission denial
 * - Launching weather forecast for a resolved location
 *
 * This design improves extensibility and reduces boilerplate, as new event types can be added
 * without breaking existing implementations (thanks to sealed interface exhaustiveness).
 *
 * @see GeoLocationCallbackEvent for available event types
 */
fun interface GeoLocationCallback {
    /**
     * Called when a geolocation-related event occurs.
     *
     * @param event The event to handle, encapsulating context and data if needed.
     */
    fun onEvent(event: GeoLocationCallbackEvent)
}

/**
 * Sealed hierarchy representing possible events emitted during geolocation workflow.
 *
 * Using a sealed interface ensures that all cases are known at compile time, allowing
 * for exhaustive `when` statements and reducing the risk of unhandled cases.
 *
 * Events include:
 * - [GotoCitySelection]: User should be navigated to city selection screen
 * - [RequestPermission]: App needs to request location permission from the user
 * - [OnPermanentlyDenied]: User permanently denied permission; consider showing guidance
 * - [OnNegativeNoPermission]: User declined permission without choosing "don't ask again"
 * - [OnForecastLoadForLocation]: Geolocation succeeded; launch forecast for the given location
 */
sealed interface GeoLocationCallbackEvent {
    /**
     * Request navigation to the city selection screen.
     * Typically triggered when geolocation is unavailable or user cancels.
     */
    data object GotoCitySelection : GeoLocationCallbackEvent

    /**
     * Request to show a system permission dialog for location access.
     * Should trigger [PermissionResolver.requestLocationPermission].
     */
    data object RequestPermission : GeoLocationCallbackEvent

    /**
     * Location permission was permanently denied (user checked "Don’t ask again").
     * Suggest redirecting to settings or showing an explanation dialog.
     */
    data object OnPermanentlyDenied : GeoLocationCallbackEvent

    /**
     * User denied location permission but can be asked again.
     * Coordinator may retry or fall back to alternative flow.
     */
    data object OnNegativeNoPermission : GeoLocationCallbackEvent

    /**
     * Geolocation completed successfully.
     * Forecast should be loaded for the provided [locationModel].
     *
     * @property locationModel The detected city and coordinates
     */
    data class OnForecastLoadForLocation(val locationModel: CityLocationModel) : GeoLocationCallbackEvent
}