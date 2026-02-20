package com.example.weatherforecast.presentation.coordinator

import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.weatherforecast.R
import com.example.weatherforecast.geolocation.PermissionResolver
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.presentation.Message
import com.example.weatherforecast.presentation.alertdialog.dialogcontroller.WeatherDialogController
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.CurrentWeatherViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationPermission
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModel
import com.example.weatherforecast.utils.ResourceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Binds forecast flows to UI, delegates dialogs/permission to controller and callbacks,
 * small focused collect methods.
 *
 * @constructor
 * @property forecastViewModel view model for weather forecast
 * @property appBarViewModel view model for user notification in AppBar
 * @property geoLocationViewModel view model for geo location
 * @property statusRenderer to show status of the app to user
 * @property dialogController to operate alert dialogs
 * @property resourceManager to get app resources
 * @property permissionResolver to request location permission from system resolver
 * @property onGotoCitySelection callback for city selection
 * @property onRequestLocationPermission callback for requesting location permission
 * @property onPermanentlyDenied callback for permanently denied location permission
 * @property onNegativeNoPermission callback for negative response to location permission
 */
class WeatherCoordinator(
    private val forecastViewModel: CurrentWeatherViewModel,
    private val appBarViewModel: AppBarViewModel,
    private val geoLocationViewModel: GeoLocationViewModel,
    private val statusRenderer: StatusRenderer,
    private val dialogController: WeatherDialogController,
    private val resourceManager: ResourceManager,
    private val permissionResolver: PermissionResolver,
    private val onGotoCitySelection: () -> Unit,
    private val onRequestLocationPermission: () -> Unit,
    private val onPermanentlyDenied: () -> Unit,
    private val onNegativeNoPermission: () -> Unit
) {

    fun startObserving(scope: CoroutineScope, lifecycle: Lifecycle) {
        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMessageFlow(forecastViewModel.messageFlow) }
                launch { forecastViewModel.gotoCitySelectionFlow.collect { onGotoCitySelection() } }
                launch { collectChosenCityNotFoundFlow(forecastViewModel.chosenCityNotFoundFlow) }
                launch { collectChosenCityBlankFlow(forecastViewModel.chosenCityBlankFlow) }
                launch { collectGeoLocationByCitySuccessFlow(geoLocationViewModel.geoLocationByCitySuccessFlow) }
                launch { collectGeoLocationSuccessFlow(geoLocationViewModel.geoLocationSuccessFlow) }
                launch { collectGeoLocationPermissionFlow(geoLocationViewModel.geoGeoLocationPermissionFlow) }
                launch { collectGeoLocationDefineCitySuccessFlow(geoLocationViewModel.geoLocationDefineCitySuccessFlow) }
                launch { geoLocationViewModel.selectCityFlow.collect { onGotoCitySelection() } }
                launch { collectForecastState(forecastViewModel.forecastState) }
            }
        }
    }

    private suspend fun collectMessageFlow(flow: SharedFlow<Message>) {
        flow.collect { statusRenderer.updateFromMessage(it) }
    }

    private suspend fun collectChosenCityNotFoundFlow(flow: SharedFlow<String>) {
        flow.collect { city ->
            statusRenderer.showWarning(
                resourceManager.getString(
                    R.string.no_selected_city_forecast,
                    city
                )
            )
            dialogController.showChosenCityNotFound(city) {
                forecastViewModel.gotoCitySelection()
            }
        }
    }

    private suspend fun collectChosenCityBlankFlow(flow: SharedFlow<Unit>) {
        flow.collect {
            statusRenderer.showStatus(resourceManager.getString(R.string.current_location_triangulating))
            geoLocationViewModel.defineCurrentGeoLocation()
        }
    }

    private suspend fun collectGeoLocationByCitySuccessFlow(flow: SharedFlow<CityLocationModel>) {
        flow.collect { location ->
            statusRenderer.showStatus(resourceManager.getString(R.string.current_location_success))
            forecastViewModel.downloadRemoteForecastForLocation(location)
        }
    }

    private suspend fun collectGeoLocationSuccessFlow(flow: SharedFlow<Location>) {
        flow.collect { location ->
            statusRenderer.showStatus(resourceManager.getString(R.string.defining_city_from_geo_location))
            geoLocationViewModel.defineCityNameByLocation(location)
        }
    }

    private suspend fun collectGeoLocationPermissionFlow(flow: SharedFlow<GeoLocationPermission>) {
        flow.collect { permission ->
            when (permission) {
                GeoLocationPermission.Requested -> {
                    statusRenderer.showStatus(resourceManager.getString(R.string.geo_location_permission_required))
                    onRequestLocationPermission()
                }

                GeoLocationPermission.Denied -> {
                    statusRenderer.showWarning(resourceManager.getString(R.string.current_location_denied))
                    dialogController.showNoPermission(
                        onPositiveClick = {
                            geoLocationViewModel.resetGeoLocationRequestAttempts()
                            permissionResolver.requestLocationPermission()
                        },
                        onNegativeClick = onNegativeNoPermission
                    )
                }

                GeoLocationPermission.Granted -> {
                    statusRenderer.showStatus(resourceManager.getString(R.string.current_location_triangulating))
                    geoLocationViewModel.defineCurrentGeoLocation()
                }

                GeoLocationPermission.PermanentlyDenied -> {
                    statusRenderer.showError(resourceManager.getString(R.string.current_location_denied_permanently))
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

    private suspend fun collectGeoLocationDefineCitySuccessFlow(flow: SharedFlow<String>) {
        flow.collect { message ->
            dialogController.showLocationDefined(
                message = message,
                onPositiveClick = { city ->
                    statusRenderer.showDownloadingStatusFor(city)
                    forecastViewModel.updateChosenCityState(city)
                    forecastViewModel.launchWeatherForecast(city)
                },
                onNegativeClick = {
                    statusRenderer.showCitySelectionStatus()
                    forecastViewModel.gotoCitySelection()
                }
            )
        }
    }

    private suspend fun collectForecastState(flow: StateFlow<WeatherUiState>) {
        flow.collect { state -> appBarViewModel.updateAppBarState(state) }
    }

    /**
     * Factory provided via [com.example.weatherforecast.di.PresentationModule];
     * ViewModels and callbacks passed in [create] from Fragment.
     */
    class Factory {
        fun create(
            forecastViewModel: CurrentWeatherViewModel,
            appBarViewModel: AppBarViewModel,
            geoLocationViewModel: GeoLocationViewModel,
            statusRenderer: StatusRenderer,
            dialogController: WeatherDialogController,
            resourceManager: ResourceManager,
            permissionResolver: PermissionResolver,
            onGotoCitySelection: () -> Unit,
            onRequestLocationPermission: () -> Unit,
            onPermanentlyDenied: () -> Unit,
            onNegativeNoPermission: () -> Unit
        ): WeatherCoordinator = WeatherCoordinator(
            forecastViewModel,
            appBarViewModel,
            geoLocationViewModel,
            statusRenderer,
            dialogController,
            resourceManager,
            permissionResolver,
            onGotoCitySelection,
            onRequestLocationPermission,
            onPermanentlyDenied,
            onNegativeNoPermission
        )
    }
}