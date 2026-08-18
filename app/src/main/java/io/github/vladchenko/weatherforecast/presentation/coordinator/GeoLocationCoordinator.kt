package io.github.vladchenko.weatherforecast.presentation.coordinator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType.Info
import io.github.vladchenko.weatherforecast.feature.geolocation.data.permission.PermissionResolver
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallback
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallbackEvent
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallbackEvent.GotoCitySelection
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallbackEvent.OnForecastLoadForLocation
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallbackEvent.RequestPermission
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel.GeoLocationEvent
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel.GeoLocationViewModel
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Acts as the coordinator for the geolocation feature, bridging the UI layer and the [GeoLocationViewModel].
 *
 * This class encapsulates the workflow for requesting, resolving, and handling location-based data.
 * It observes state flows from the [GeoLocationViewModel] and triggers appropriate UI responses
 * such as status updates, permission dialogs, or navigation events.
 *
 * ## Key Responsibilities
 * - **State Observation**: Observes [SharedFlow]s from [GeoLocationViewModel] to react to geolocation events.
 * - **Permission Management**: Collaborates with [PermissionResolver] to handle Android runtime permissions.
 * - **User Guidance**: Uses [WeatherDialogController] and [StatusStateHolder] to guide the user through
 *   location detection, permission denials, and city confirmation.
 * - **Event Propagation**: Sends callback events via [GeoLocationCallback] when specific actions occur (e.g., location loaded, user denied).
 *
 * ## Workflow
 * 1. [startObserving] initiates flow collection when the UI component is in [Lifecycle.State.STARTED].
 * 2. [startGeoLocation] triggers the process in the ViewModel.
 * 3. The ViewModel updates flows (e.g., permission state, location updates).
 * 4. This coordinator intercepts flows and handles UI logic (e.g., requesting permission, asking for city confirmation).
 *
 * ## Thread Safety
 * Designed to be used within a coroutine scope tied to the UI lifecycle. All flows are observed
 * on the main thread. State updates are sent via [StatusStateHolder] which manages the UI status.
 *
 * @property callback Interface for sending high-level events to the UI (e.g., load forecast, go to search).
 * @property resourceManager Provides string resources for UI messages and dialogs.
 * @property permissionResolver Handles the logic for requesting and checking location permissions.
 * @property statusStateHolder Updates the global status bar (e.g., "Detecting location...", "City found").
 * @property dialogController Manages the presentation of confirmation and error dialogs.
 * @property geoLocationViewModel Source of geolocation state and triggers.
 */
class GeoLocationCoordinator(
    private val callback: GeoLocationCallback,
    private val resourceManager: ResourceManager,
    private val permissionResolver: PermissionResolver,
    private val statusStateHolder: StatusStateHolder,
    private val dialogController: WeatherDialogController,
    private val geoLocationViewModel: GeoLocationViewModel
) {

    /**
     * Starts observing the unified geolocation event stream.
     * Consolidates all geolocation workflow events
     * (permissions, location updates, city resolution) into a single observation point.
     *
     * @param scope The coroutine scope in which to launch observation
     * @param lifecycle The lifecycle to tie observation duration to
     */
    fun startObserving(scope: CoroutineScope, lifecycle: Lifecycle) {
        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectGeoLocationEvents(geoLocationViewModel.geoLocationEventsFlow) }
            }
        }
    }

    private suspend fun collectGeoLocationEvents(eventsFlow: SharedFlow<GeoLocationEvent>) {
        eventsFlow.collect { event ->
            when (event) {
                is GeoLocationEvent.DeviceGeoLocationSuccess -> {
                    geoLocationViewModel.defineCityNameByLocation(event.location)
                }

                is GeoLocationEvent.DefineCityNameByLocationSuccess -> {
                    dialogController.showLocationDefined(
                        city = event.cityModel.city,
                        onPositiveClick = {
                            callback.onEvent(
                                OnForecastLoadForLocation(event.cityModel)
                            )
                        },
                        onNegativeClick = {
                            statusStateHolder.updateStatus(
                                Info(resourceManager.getString(R.string.city_selection_title))
                            )
                            callback.onEvent(
                                GotoCitySelection
                            )
                        }
                    )
                }

                is GeoLocationEvent.DefineLocationFail -> {
                    dialogController.showGeoLocationError(
                        onPositiveClick = {
                            callback.onEvent(
                                GotoCitySelection
                            )
                        },
                        onNegativeClick = {
                            startGeoLocation()
                        }
                    )
                }
                is GeoLocationEvent.GeoLocationPermission.Requested -> {
                    callback.onEvent(RequestPermission)
                }

                is GeoLocationEvent.GeoLocationPermission.Denied -> {
                    dialogController.showNoPermission(
                        onPositiveClick = {
                            permissionResolver.requestLocationPermission()
                        },
                        onNegativeClick = {
                            callback.onEvent(
                                GeoLocationCallbackEvent.OnNegativeNoPermission
                            )
                        }
                    )
                }

                is GeoLocationEvent.GeoLocationPermission.Granted -> {
                    geoLocationViewModel.defineDeviceGeoLocation()
                }

                is GeoLocationEvent.GeoLocationPermission.PermanentlyDenied -> {
                    dialogController.showPermissionPermanentlyDenied(
                        onPositiveClick = {
                            callback.onEvent(
                                GeoLocationCallbackEvent.OnPermanentlyDenied
                            )
                        },
                        onNegativeClick = {
                            callback.onEvent(
                                GeoLocationCallbackEvent.OnPermanentlyDenied
                            )
                        }
                    )
                }

                is GeoLocationEvent.SelectCity -> {
                    callback.onEvent(
                        GotoCitySelection
                    )
                }
            }
        }
    }

    /**
     * Initiates the geolocation process.
     *
     * This is the entry point for the user to request their current location.
     * It performs two actions:
     * 1. Updates the [StatusStateHolder] to display an informational message ("Detecting location...").
     * 2. Delegates the actual logic to [GeoLocationViewModel.defineDeviceGeoLocation],
     *    which will likely trigger permission requests or location updates.
     */
    fun startGeoLocation() {
        statusStateHolder.updateStatus(
            Info(resourceManager.getString(R.string.geo_detecting))
        )
        geoLocationViewModel.defineDeviceGeoLocation()
    }

    /**
     * Factory class for creating configured instances of [GeoLocationCoordinator].
     *
     * Encapsulates dependency injection logic and ensures proper wiring of internal components,
     * promoting loose coupling and testability.
     */
    class Factory {
        /**
         * Creates and returns a fully configured [GeoLocationCoordinator] instance.
         *
         * Initializes the coordinator with all required dependencies:
         * - [GeoLocationCallback] for event propagation to the UI layer.
         * - [ResourceManager] for localized string resources.
         * - [PermissionResolver] for handling runtime location permissions.
         * - [StatusStateHolder] for broadcasting UI status updates.
         * - [WeatherDialogController] for managing dialog presentation.
         * - [GeoLocationViewModel] as the source of geolocation state.
         *
         * @param callback Interface for sending geolocation events to the UI.
         * @param resourceManager Provides localized string resources.
         * @param permissionResolver Handles runtime location permission requests.
         * @param statusStateHolder Manages and broadcasts UI status updates.
         * @param dialogController Manages presentation of alert dialogs.
         * @param geoLocationViewModel Source of geolocation state and operations.
         * @return A fully initialized [GeoLocationCoordinator] instance.
         */
        fun create(
            callback: GeoLocationCallback,
            resourceManager: ResourceManager,
            permissionResolver: PermissionResolver,
            statusStateHolder: StatusStateHolder,
            dialogController: WeatherDialogController,
            geoLocationViewModel: GeoLocationViewModel
        ): GeoLocationCoordinator {
            return GeoLocationCoordinator(
                callback = callback,
                resourceManager = resourceManager,
                dialogController = dialogController,
                permissionResolver = permissionResolver,
                statusStateHolder = statusStateHolder,
                geoLocationViewModel = geoLocationViewModel
            )
        }
    }
}