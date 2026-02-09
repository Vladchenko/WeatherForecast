package com.example.weatherforecast.presentation.coordinator

import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.weatherforecast.R
import com.example.weatherforecast.geolocation.PermissionResolver
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.presentation.Message
import com.example.weatherforecast.presentation.alertdialog.dialogcontroller.ForecastDialogController
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.ForecastUiState
import com.example.weatherforecast.presentation.viewmodel.forecast.HourlyForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
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
 */
class ForecastCoordinator(
    private val forecastViewModel: WeatherForecastViewModel,
    private val appBarViewModel: AppBarViewModel,
    private val geoLocationViewModel: GeoLocationViewModel,
    private val hourlyForecastViewModel: HourlyForecastViewModel,
    private val statusRenderer: StatusRenderer,
    private val dialogController: ForecastDialogController,
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
                launch { collectCityRequestFailedFlow(forecastViewModel.cityRequestFailedFlow) }
                launch { forecastViewModel.gotoCitySelectionFlow.collect { onGotoCitySelection() } }
                launch { collectChosenCityNotFoundFlow(forecastViewModel.chosenCityNotFoundFlow) }
                launch { collectChosenCityBlankFlow(forecastViewModel.chosenCityBlankFlow) }
                launch { collectGeoLocationByCitySuccessFlow(geoLocationViewModel.geoLocationByCitySuccessFlow) }
                launch { collectGeoLocationSuccessFlow(geoLocationViewModel.geoLocationSuccessFlow) }
                launch { collectGeoLocationPermissionFlow(geoLocationViewModel.geoGeoLocationPermissionFlow) }
                launch { collectGeoLocationDefineCitySuccessFlow(geoLocationViewModel.geoLocationDefineCitySuccessFlow) }
                launch { geoLocationViewModel.selectCityFlow.collect { onGotoCitySelection() } }
                launch { collectRemoteRequestFailedFlow(hourlyForecastViewModel.remoteRequestFailedFlow) }
                launch { collectForecastState(forecastViewModel.forecastState) }
            }
        }
    }

    private suspend fun collectMessageFlow(flow: SharedFlow<Message>) {
        flow.collect { statusRenderer.updateFromMessage(it) }
    }

    private suspend fun collectCityRequestFailedFlow(flow: SharedFlow<String>) {
        flow.collect { city ->
            statusRenderer.showStatus(resourceManager.getString(R.string.geo_location_by_city_define, city))
            geoLocationViewModel.defineLocationByCity(city)
        }
    }

    private suspend fun collectChosenCityNotFoundFlow(flow: SharedFlow<String>) {
        flow.collect { city ->
            statusRenderer.showStatus(resourceManager.getString(R.string.selected_city_not_found))
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
                        onPositive = {
                            geoLocationViewModel.resetGeoLocationRequestAttempts()
                            permissionResolver.requestLocationPermission()
                        },
                        onNegative = onNegativeNoPermission
                    )
                }

                GeoLocationPermission.Granted -> {
                    statusRenderer.showStatus(resourceManager.getString(R.string.current_location_triangulating))
                    geoLocationViewModel.defineCurrentGeoLocation()
                }

                GeoLocationPermission.PermanentlyDenied -> {
                    statusRenderer.showError(resourceManager.getString(R.string.current_location_denied_permanently))
                    dialogController.showPermissionPermanentlyDenied(
                        onPositive = {
                            geoLocationViewModel.resetGeoLocationRequestAttempts()
                            permissionResolver.requestLocationPermission()
                        },
                        onNegative = onPermanentlyDenied
                    )
                }
            }
        }
    }

    private suspend fun collectGeoLocationDefineCitySuccessFlow(flow: SharedFlow<String>) {
        flow.collect { message ->
            dialogController.showLocationDefined(
                message = message,
                onPositive = { city ->
                    statusRenderer.showDownloadingStatusFor(city)
                    forecastViewModel.updateChosenCityState(city)
                    forecastViewModel.launchWeatherForecast(city)
                },
                onNegative = {
                    statusRenderer.showCitySelectionStatus()
                    forecastViewModel.gotoCitySelection()
                }
            )
        }
    }

    private suspend fun collectRemoteRequestFailedFlow(flow: SharedFlow<String>) {
        flow.collect { hourlyForecastViewModel.getLocalCity() }
    }

    private suspend fun collectForecastState(flow: StateFlow<ForecastUiState>) {
        flow.collect { state -> appBarViewModel.updateAppBarState(state) }
    }

    /**
     * Factory provided via [com.example.weatherforecast.di.ForecastPresentationModule];
     * ViewModels and callbacks passed in [create] from Fragment.
     */
    class Factory {
        fun create(
            forecastViewModel: WeatherForecastViewModel,
            appBarViewModel: AppBarViewModel,
            geoLocationViewModel: GeoLocationViewModel,
            hourlyForecastViewModel: HourlyForecastViewModel,
            statusRenderer: StatusRenderer,
            dialogController: ForecastDialogController,
            resourceManager: ResourceManager,
            permissionResolver: PermissionResolver,
            onGotoCitySelection: () -> Unit,
            onRequestLocationPermission: () -> Unit,
            onPermanentlyDenied: () -> Unit,
            onNegativeNoPermission: () -> Unit
        ): ForecastCoordinator = ForecastCoordinator(
            forecastViewModel,
            appBarViewModel,
            geoLocationViewModel,
            hourlyForecastViewModel,
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