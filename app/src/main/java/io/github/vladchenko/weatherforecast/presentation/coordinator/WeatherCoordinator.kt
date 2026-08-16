package io.github.vladchenko.weatherforecast.presentation.coordinator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.geolocation.data.permission.PermissionResolver
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallback
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallbackEvent
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel.GeoLocationViewModel
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogController
import kotlinx.coroutines.CoroutineScope

/**
 * Coordinates high-level UI logic for the weather forecast screen.
 *
 * Acts as the central orchestrator that connects view models with presentation components,
 * translating state changes into appropriate UI actions. Designed to keep fragments and activities
 * free of coordination logic, improving testability and separation of concerns.
 *
 * ## Responsibilities
 * - Observes message flows from weather view models and updates status via [StatusRenderer]
 * - Synchronizes app bar state with current weather UI state
 * - Delegates specialized workflows to dedicated coordinators:
 *   - [GeoLocationCoordinator] handles location-based forecast retrieval
 *   - [CitySelectionCoordinator] manages fallback strategies for invalid or missing city input
 *
 * ## Lifecycle Management
 * Uses [repeatOnLifecycle] to ensure observation occurs only during active lifecycle states,
 * preventing memory leaks and unnecessary processing. Must be started with [startObserving].
 *
 * ## Thread Safety
 * All coroutine collections occur on the main thread via [lifecycle.repeatOnLifecycle],
 * ensuring safe access to UI components.
 *
 * @property geoLocationCoordinator Handles geolocation-specific workflow and user interactions
 * @property citySelectionCoordinator Manages responses to blank or invalid city input
 */
class WeatherCoordinator private constructor(
    private val geoLocationCoordinator: GeoLocationCoordinator,
    private val citySelectionCoordinator: CitySelectionCoordinator
) {

    /**
     * Starts observing all relevant state and event flows from associated view models.
     *
     * Launches concurrent coroutines to collect:
     * - Message events from both current and hourly weather view models
     * - Changes in overall weather UI state for app bar synchronization
     *
     * Also initializes observation in:
     * - [geoLocationCoordinator] — for location-based forecast workflow
     * - [citySelectionCoordinator] — for handling missing or invalid city input
     *
     * Observation is scoped to [Lifecycle.State.STARTED] to prevent unnecessary work
     * while the UI is not visible.
     *
     * @param scope The coroutine scope used to launch observation tasks
     * @param lifecycle The lifecycle to which observation is bound
     */
    fun startObserving(scope: CoroutineScope, lifecycle: Lifecycle) {
        geoLocationCoordinator.startObserving(scope, lifecycle)
        citySelectionCoordinator.startObserving(scope, lifecycle)
    }

    /**
     * Factory class for creating configured instances of [WeatherCoordinator].
     *
     * Encapsulates dependency injection logic and ensures proper wiring of internal components,
     * including creation of:
     * - [GeoLocationCoordinator]
     * - [CitySelectionCoordinator]
     *
     * Promotes loose coupling and testability by allowing full control over dependencies.
     */
    class Factory {
        /**
         * Creates and returns a fully configured [WeatherCoordinator] instance.
         *
         * Initializes both [GeoLocationCoordinator] and [CitySelectionCoordinator]
         * with appropriate callbacks and shared dependencies.
         *
         * @param callback on geo location events
         * @param resourceManager Accessor for localized string resources
         * @param permissionResolver Handles runtime location permission requests
         * @param statusStateHolder Manages and broadcasts UI status updates (loading, errors, info)
         * @param dialogController Manages presentation of alert dialogs
         * @param forecastViewModel Main source of current weather data and user actions
         * @param geoLocationViewModel Provides geolocation state and operations
         * @return A fully initialized and wired [WeatherCoordinator] instance
         */
        fun create(
            callback: GeoLocationCallback,
            resourceManager: ResourceManager,
            permissionResolver: PermissionResolver,
            statusStateHolder: StatusStateHolder,
            dialogController: WeatherDialogController,
            forecastViewModel: CurrentWeatherViewModel,
            geoLocationViewModel: GeoLocationViewModel
        ): WeatherCoordinator {
            val geoLocationCoordinator = GeoLocationCoordinator(
                callback = callback,
                resourceManager = resourceManager,
                dialogController = dialogController,
                permissionResolver = permissionResolver,
                statusStateHolder = statusStateHolder,
                geoLocationViewModel = geoLocationViewModel
            )

            val citySelectionCoordinator = CitySelectionCoordinator(
                resourceManager = resourceManager,
                dialogController = dialogController,
                forecastViewModel = forecastViewModel,
                statusStateHolder = statusStateHolder,
                geoLocationCoordinator = geoLocationCoordinator,
                onGotoCitySelection = { callback.onEvent(GeoLocationCallbackEvent.GotoCitySelection) }
            )

            return WeatherCoordinator(
                geoLocationCoordinator = geoLocationCoordinator,
                citySelectionCoordinator = citySelectionCoordinator
            )
        }
    }
}