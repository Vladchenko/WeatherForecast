package com.example.weatherforecast.presentation.coordinator

import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.weatherforecast.R
import com.example.weatherforecast.geolocation.PermissionResolver
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.presentation.alertdialog.dialogcontroller.WeatherDialogController
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationPermission
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModel
import com.example.weatherforecast.utils.ResourceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Coordinates geolocation-related logic between UI components and business logic.
 *
 * This class acts as a mediator that observes flows from [GeoLocationViewModel] and translates them
 * into UI actions such as showing status messages, displaying dialogs, or triggering navigation.
 * It encapsulates the coordination logic to keep activities and fragments clean and testable.
 *
 * ## Responsibilities
 * - Observes geolocation-related state changes via [SharedFlow]s from [GeoLocationViewModel]
 * - Triggers appropriate UI responses (status rendering, dialogs) based on state
 * - Handles permission resolution workflow in collaboration with [PermissionResolver]
 * - Delegates navigation and result delivery to callback functions
 *
 * ## Lifecycle Management
 * Uses [repeatOnLifecycle] to ensure observation only occurs during active lifecycle states,
 * preventing memory leaks and unnecessary processing.
 *
 * ## Thread Safety
 * Designed to be used within a coroutine scope tied to the UI lifecycle. All collected flows
 * are observed on the main thread unless otherwise specified in the view model.
 *
 * @property geoLocationViewModel Provides geolocation state and operations
 * @property permissionResolver Handles runtime location permission requests
 * @property statusRenderer Displays loading, success, warning, or error statuses
 * @property dialogController Manages presentation of alert dialogs
 * @property resourceManager Accesses string resources for UI messages
 * @property onGotoCitySelection Callback invoked when user should be navigated to city selection screen
 * @property onRequestLocationPermission Callback triggered when location permission needs to be requested
 * @property onPermanentlyDenied Callback for handling permanent denial of location permission
 * @property onNegativeNoPermission Callback for when user declines permission without retrying
 * @property onForecastLoadForLocation Callback invoked with resolved city location to load forecast
 */
class GeoLocationCoordinator(
    private val geoLocationViewModel: GeoLocationViewModel,
    private val permissionResolver: PermissionResolver,
    private val statusRenderer: StatusRenderer,
    private val dialogController: WeatherDialogController,
    private val resourceManager: ResourceManager,
    private val onGotoCitySelection: () -> Unit,
    private val onRequestLocationPermission: () -> Unit,
    private val onPermanentlyDenied: () -> Unit,
    private val onNegativeNoPermission: () -> Unit,
    private val onForecastLoadForLocation: (CityLocationModel) -> Unit
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
                launch { geoLocationViewModel.selectCityFlow.collect { onGotoCitySelection() } }
            }
        }
    }

    /**
     * Initiates the process of determining the user's current geographic location.
     *
     * Updates UI status to indicate location triangulation is in progress and
     * delegates the actual operation to [GeoLocationViewModel.defineCurrentGeoLocation].
     */
    fun startGeoLocation() {
        statusRenderer.showStatus(resourceManager.getString(R.string.geo_detecting))
        geoLocationViewModel.defineCurrentGeoLocation()
    }

    /**
     * Collects successful emissions from [geoLocationViewModel.geoLocationByCitySuccessFlow].
     *
     * Upon receiving a [CityLocationModel], updates the UI to reflect successful location resolution
     * and triggers forecast loading for the resolved city via [onForecastLoadForLocation].
     *
     * @param flow The shared flow emitting city location data upon successful lookup
     */
    private suspend fun collectGeoLocationByCitySuccessFlow(flow: SharedFlow<CityLocationModel>) {
        flow.collect { model ->
            statusRenderer.showStatus(resourceManager.getString(R.string.geo_success))
            onForecastLoadForLocation(model)
        }
    }

    /**
     * Collects successful location readings from the device's location provider.
     *
     * When a [Location] is received, updates status to indicate city name resolution
     * and requests the view model to determine the corresponding city name.
     *
     * @param flow The shared flow emitting raw location coordinates
     */
    private suspend fun collectGeoLocationSuccessFlow(flow: SharedFlow<Location>) {
        flow.collect { location ->
            statusRenderer.showStatus(resourceManager.getString(R.string.geo_finding_city))
            geoLocationViewModel.defineCityNameByLocation(location)
        }
    }

    /**
     * Observes changes in location permission state and reacts accordingly.
     *
     * Handles four states:
     * - [GeoLocationPermission.Requested]: Shows rationale and triggers permission request
     * - [GeoLocationPermission.Denied]: Shows warning and offers retry option
     * - [GeoLocationPermission.Granted]: Proceeds with location retrieval
     * - [GeoLocationPermission.PermanentlyDenied]: Shows permanent denial dialog with guidance
     *
     * @param flow The shared flow emitting permission state updates
     */
    private suspend fun collectGeoLocationPermissionFlow(flow: SharedFlow<GeoLocationPermission>) {
        flow.collect { permission ->
            when (permission) {
                GeoLocationPermission.Requested -> {
                    statusRenderer.showStatus(resourceManager.getString(R.string.geo_permission_required))
                    onRequestLocationPermission()
                }

                GeoLocationPermission.Denied -> {
                    statusRenderer.showWarning(resourceManager.getString(R.string.geo_permission_denied))
                    dialogController.showNoPermission(
                        onPositiveClick = {
                            geoLocationViewModel.resetGeoLocationRequestAttempts()
                            permissionResolver.requestLocationPermission()
                        },
                        onNegativeClick = onNegativeNoPermission
                    )
                }

                GeoLocationPermission.Granted -> {
                    statusRenderer.showStatus(resourceManager.getString(R.string.geo_detecting))
                    geoLocationViewModel.defineCurrentGeoLocation()
                }

                GeoLocationPermission.PermanentlyDenied -> {
                    statusRenderer.showError(resourceManager.getString(R.string.geo_permission_denied_permanently))
                    dialogController.showPermissionPermanentlyDenied(
                        onPositiveClick = {
                            geoLocationViewModel.resetGeoLocationRequestAttempts()
                            permissionResolver.requestLocationPermission()
                        },
                        onNegativeClick = onPermanentlyDenied
                    )
                }
            }
        }
    }

    /**
     * Collects successfully resolved city names from geographic coordinates.
     *
     * When a city name is determined, shows a confirmation dialog allowing the user
     * to accept or reject the detected location. Acceptance leads to forecast loading
     * (handled by other flows), while rejection navigates to manual city selection.
     *
     * @param flow The shared flow emitting resolved city names
     */
    private suspend fun collectGeoLocationDefineCitySuccessFlow(flow: SharedFlow<String>) {
        flow.collect { cityName ->
            dialogController.showLocationDefined(
                message = cityName,
                onPositiveClick = { selectedCity ->
                    // Don't do anything here
                    // Downloading is to begin in geoLocationByCitySuccessFlow
                },
                onNegativeClick = {
                    statusRenderer.showCitySelectionStatus()
                    onGotoCitySelection()
                }
            )
        }
    }
}