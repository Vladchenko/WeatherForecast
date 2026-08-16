package io.github.vladchenko.weatherforecast.presentation.coordinator

import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.status.StatusStateHolder
import io.github.vladchenko.weatherforecast.core.ui.status.StatusType
import io.github.vladchenko.weatherforecast.feature.geolocation.data.permission.PermissionResolver
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallback
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallbackEvent
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.model.GeoLocationPermission
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
     * Starts observing all relevant flows from the [GeoLocationViewModel].
     *
     * Launches multiple coroutines within the provided [CoroutineScope] to collect:
     * - Geolocation success events
     * - Permission state changes
     * - City definition results
     * - Navigation requests to city selection
     *
     * Observation is scoped to the [Lifecycle.State.STARTED] state to prevent unnecessary work
     * while the associated component is stopped.
     *
     * @param scope The coroutine scope in which to launch observation
     * @param lifecycle The lifecycle to tie observation duration to
     */
    fun startObserving(scope: CoroutineScope, lifecycle: Lifecycle) {
        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectGeoLocationByCitySuccessFlow(geoLocationViewModel.geoLocationByCitySuccessFlow) }
                launch { collectGeoLocationSuccessFlow(geoLocationViewModel.geoLocationSuccessFlow) }
                launch { collectGeoLocationPermissionFlow(geoLocationViewModel.geoGeoLocationPermissionFlow) }
                launch { collectGeoLocationDefineCitySuccessFlow(geoLocationViewModel.geoLocationDefineCitySuccessFlow) }
                launch {
                    geoLocationViewModel.selectCityFlow.collect {
                        callback.onEvent(
                            GeoLocationCallbackEvent.GotoCitySelection
                        )
                    }
                }
                launch { collectGeoLocationErrorFlow(geoLocationViewModel.geoLocationFailFlow) }
            }
        }
    }

    /**
     * Initiates the geolocation process.
     *
     * This is the entry point for the user to request their current location.
     * It performs two actions:
     * 1. Updates the [StatusStateHolder] to display an informational message ("Detecting location...").
     * 2. Delegates the actual logic to [GeoLocationViewModel.defineCurrentGeoLocation],
     *    which will likely trigger permission requests or location updates.
     */
    fun startGeoLocation() {
        statusStateHolder.updateStatus(
            StatusType.Info(resourceManager.getString(R.string.geo_detecting))
        )
        geoLocationViewModel.defineCurrentGeoLocation()
    }

    /**
     * Handles the event when a city is successfully found via manual search.
     *
     * Updates the status message to "Success" to indicate that the selected city is now being processed
     * for weather loading (typically delegated to a parent listener or callback).
     *
     * @param flow The [SharedFlow] emitting success events for city lookup.
     */
    private suspend fun collectGeoLocationByCitySuccessFlow(flow: SharedFlow<Unit>) {
        flow.collect {
            statusStateHolder.updateStatus(
                StatusType.Info(resourceManager.getString(R.string.geo_success))
            )
        }
    }

    /**
     * Handles successful raw location updates from the device.
     *
     * When a new [Location] (GPS/Network) is received, this method:
     * 1. Updates the status to "Finding city..." to inform the user that reverse geocoding is happening.
     * 2. Calls [GeoLocationViewModel.defineCityNameByLocation] to convert coordinates to a city name.
     *
     * @param flow The [SharedFlow] emitting raw [Location] updates.
     */
    private suspend fun collectGeoLocationSuccessFlow(flow: SharedFlow<Location>) {
        flow.collect { location ->
            statusStateHolder.updateStatus(
                StatusType.Info(resourceManager.getString(R.string.geo_finding_city))
            )
            geoLocationViewModel.defineCityNameByLocation(location)
        }
    }

    /**
     * Observes and handles changes in the location permission state.
     *
     * Implements the full permission lifecycle logic:
     * - [GeoLocationPermission.Requested]: The app asks for permission for the first time. Updates status
     *   and triggers the system permission dialog via [PermissionResolver.requestLocationPermission].
     * - [GeoLocationPermission.Denied]: The user denied permission (not permanently). Shows a warning status
     *   and a dialog offering to request permission again.
     * - [GeoLocationPermission.Granted]: The user granted permission. Updates status to "Detecting" and starts
     *   the location process.
     * - [GeoLocationPermission.PermanentlyDenied]: The user checked "Don't ask again". Shows a permanent
     *   error status and a dialog explaining they need to go to settings.
     *
     * @param flow The [SharedFlow] emitting permission state changes.
     */
    private suspend fun collectGeoLocationPermissionFlow(flow: SharedFlow<GeoLocationPermission>) {
        flow.collect { permission ->
            when (permission) {
                GeoLocationPermission.Requested -> {
                    statusStateHolder.updateStatus(
                        StatusType.Info(resourceManager.getString(R.string.geo_permission_required))
                    )
                    callback.onEvent(GeoLocationCallbackEvent.RequestPermission)
                }

                GeoLocationPermission.Denied -> {
                    statusStateHolder.updateStatus(
                        StatusType.Warning(resourceManager.getString(R.string.geo_permission_denied))
                    )
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

                GeoLocationPermission.Granted -> {
                    statusStateHolder.updateStatus(
                        StatusType.Info(resourceManager.getString(R.string.geo_detecting))
                    )
                    geoLocationViewModel.defineCurrentGeoLocation()
                }

                GeoLocationPermission.PermanentlyDenied -> {
                    statusStateHolder.updateStatus(
                        StatusType.Error(resourceManager.getString(R.string.geo_permission_denied_permanently))
                    )
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
            }
        }
    }

    /**
     * Handles the successful resolution of a city name from geographic coordinates.
     *
     * When [GeoLocationViewModel] successfully determines the city name from GPS data,
     * this method displays a confirmation dialog ([WeatherDialogController.showLocationDefined]).
     *
     * The user is presented with the detected city name and two choices:
     * - **Accept**: Sends [GeoLocationCallbackEvent.OnForecastLoadForLocation] to proceed with weather loading.
     * - **Reject**: Updates status and sends [GeoLocationCallbackEvent.GotoCitySelection] to manually search.
     *
     * @param flow The [SharedFlow] emitting [CityLocationModel] objects.
     */
    private suspend fun collectGeoLocationDefineCitySuccessFlow(flow: SharedFlow<CityLocationModel>) {
        flow.collect { model ->
            dialogController.showLocationDefined(
                city = model.city,
                onPositiveClick = {
                    callback.onEvent(
                        GeoLocationCallbackEvent.OnForecastLoadForLocation(model)
                    )
                },
                onNegativeClick = {
                    statusStateHolder.updateStatus(
                        StatusType.Info(resourceManager.getString(R.string.city_selection_title))
                    )
                    callback.onEvent(
                        GeoLocationCallbackEvent.GotoCitySelection
                    )
                }
            )
        }
    }

    /**
     * Handles error events during the geolocation process.
     *
     * Displays an error dialog ([WeatherDialogController.showGeoLocationError]) indicating that
     * the location could not be determined.
     *
     * - **Accept**: Navigates to manual city selection.
     * - **Cancel**: Retries the geolocation process via [startGeoLocation].
     *
     * @param flow The [SharedFlow] emitting error events.
     */
    private suspend fun collectGeoLocationErrorFlow(flow: SharedFlow<Unit>) {
        flow.collect {
            dialogController.showGeoLocationError(
                onPositiveClick = {
                    callback.onEvent(
                        GeoLocationCallbackEvent.GotoCitySelection
                    )
                },
                onNegativeClick = {
                    startGeoLocation()
                }
            )
        }
    }
}