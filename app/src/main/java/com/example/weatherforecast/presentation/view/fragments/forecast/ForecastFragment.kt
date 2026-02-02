package com.example.weatherforecast.presentation.view.fragments.forecast

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weatherforecast.R
import com.example.weatherforecast.models.presentation.Message
import com.example.weatherforecast.presentation.PresentationUtils.closeWith
import com.example.weatherforecast.presentation.view.fragments.forecast.current.CurrentTimeForecastLayout
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.HourlyForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationPermission
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

/**
 * Fragment representing a weather forecast.
 */
@AndroidEntryPoint
class ForecastFragment : Fragment() {

    private var mainView: View? = null
    private val arguments: ForecastFragmentArgs by navArgs()
    private val dialogHelper by lazy { AlertDialogHelper(requireContext()) }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted ->
            geoLocationViewModel.onPermissionResolution(isGranted)
        }
    private val forecastViewModel by activityViewModels<WeatherForecastViewModel>()
    private val appBarViewModel by activityViewModels<AppBarViewModel>()
    private val geoLocationViewModel by activityViewModels<GeoLocationViewModel>()
    private val hourlyForecastViewModel by activityViewModels<HourlyForecastViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Пока нет данных — можно показать заглушку или пустой экран
            setContent {
                // Будет обновляться динамически в onViewCreated
                // TODO Отобразить заглушку
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainView = view
        super.onViewCreated(view, savedInstanceState)
        initFlowObservers()
        initAppBarObserver()
        val chosenCity = arguments.chosenCity
        showInitialDownloadMessage(chosenCity)
        forecastViewModel.launchWeatherForecast(chosenCity)

        (view as ComposeView).setContent {
            CurrentTimeForecastLayout(
                mainContentTextColor = Color.Black,
                onCityClick = {
                    gotoCitySelectionScreen()
                },
                onBackClick = {
                    activity?.finish()
                },
                appBarViewModel = appBarViewModel,
                viewModel = forecastViewModel,
                hourlyViewModel = hourlyForecastViewModel
            )
        }
    }

    private fun showInitialDownloadMessage(chosenCity: String) {
        if (chosenCity.isBlank()) {
            appBarViewModel.updateSubtitle(
                getString(
                    R.string.forecast_downloading_for_city_text,
                    chosenCity
                )
            )
        } else {
            appBarViewModel.updateSubtitle(getString(R.string.forecast_downloading))
        }
    }

    private fun initFlowObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    forecastViewModel.messageFlow.collect {
                        when (it) {
                            is Message.Success -> {
                                when (it.content) {
                                    is Message.Content.Text -> {
                                        appBarViewModel.updateSubtitle(it.content.message)
                                    }
                                    is Message.Content.Resource -> {
                                        appBarViewModel.updateSubtitle(
                                            getString(it.content.resId)
                                        )
                                    }
                                }
                            }
                            is Message.Error -> {
                                when (it.content) {
                                    is Message.Content.Text -> {
                                        appBarViewModel.updateSubtitleWithError(
                                            it.content.message
                                        )
                                    }
                                    is Message.Content.Resource -> {
                                        appBarViewModel.updateSubtitleWithError(
                                            getString(it.content.resId)
                                        )
                                    }
                                }
                            }
                            is Message.Warning -> {
                                when (it.content) {
                                    is Message.Content.Text -> {
                                        appBarViewModel.updateSubtitleWithWarning(
                                            it.content.message
                                        )
                                    }
                                    is Message.Content.Resource -> {
                                        appBarViewModel.updateSubtitleWithWarning(
                                            getString(it.content.resId)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                launch {
                    forecastViewModel.cityRequestFailedFlow.collect {
                        appBarViewModel.updateSubtitle(
                            getString(R.string.geo_location_by_city_define, it)
                        )
                        geoLocationViewModel.defineLocationByCity(it)
                    }
                }
                launch {
                    forecastViewModel.gotoCitySelectionFlow.collect { gotoCitySelectionScreen() }
                }
                launch {
                    forecastViewModel.chosenCityNotFoundFlow.collect { city ->
                        mainView?.run {
                            val alertDialog = dialogHelper.getAlertDialogBuilderToChooseAnotherCity(
                                city,
                                onPositiveClick = { forecastViewModel.gotoCitySelection() },
                                onNegativeClick = { /*pass*/ }
                            ).show()
                            alertDialog.setCancelable(false)
                            alertDialog.setCanceledOnTouchOutside(false)
                            alertDialog.closeWith(this)
                        }
                    }
                }
                launch {
                    forecastViewModel.chosenCityBlankFlow.collect {
                        appBarViewModel.updateSubtitle(getString(R.string.current_location_triangulating))
                        geoLocationViewModel.defineCurrentGeoLocation()
                    }
                }
                launch {
                    geoLocationViewModel.geoLocationByCitySuccessFlow.collect {
                        appBarViewModel.updateSubtitle(getString(R.string.current_location_success))
                        forecastViewModel.downloadRemoteForecastForLocation(it)
                    }
                }
                launch {
                    geoLocationViewModel.geoLocationSuccessFlow.collect {
                        appBarViewModel.updateSubtitle(getString(R.string.defining_city_from_geo_location))
                        geoLocationViewModel.defineCityNameByLocation(it)
                    }
                }
                launch {
                    geoLocationViewModel.geoGeoLocationPermissionFlow.collect {
                        when (it) {
                            GeoLocationPermission.Requested -> {
                                appBarViewModel.updateSubtitle(
                                    getString(R.string.geo_location_permission_required)
                                )
                                requestLocationPermission()
                            }
                            GeoLocationPermission.Denied -> {
                                appBarViewModel.updateSubtitle(
                                    getString(R.string.current_location_denied)
                                )
                                showNoPermissionAlertDialog()
                            }
                            GeoLocationPermission.Granted -> {
                                appBarViewModel.updateSubtitle(
                                    getString(R.string.current_location_triangulating)
                                )
                                geoLocationViewModel.defineCurrentGeoLocation()
                            }
                            GeoLocationPermission.PermanentlyDenied -> {
                                appBarViewModel.updateSubtitle(
                                getString(R.string.current_location_denied_permanently)
                                )
                                showToastAndOpenAppSettings()
                            }
                        }
                    }
                }
                launch {
                    geoLocationViewModel.geoLocationDefineCitySuccessFlow.collect {
                        locationDefinedAlertDialog(it)
                    }
                }
                launch {
                    geoLocationViewModel.selectCityFlow.collect {
                        gotoCitySelectionScreen()
                    }
                }
                launch {
                    hourlyForecastViewModel.remoteRequestFailedFlow.collect {
                        hourlyForecastViewModel.getLocalCity()
                    }
                }
            }
        }
    }

    private fun initAppBarObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    forecastViewModel.forecastState.collect { state ->
                        appBarViewModel.updateAppBarState(state)
                    }
                }
            }
        }
    }

    private fun locationDefinedAlertDialog(message: String) {
        mainView?.run {
            val alertDialog = dialogHelper.getGeoLocationAlertDialogBuilder(
                message,
                onPositiveClick = {
                    showStatusDependingOnCity(it)
                    forecastViewModel.updateChosenCityState(it)
                    forecastViewModel.launchWeatherForecast(it)
                },
                onNegativeClick = {
                    appBarViewModel.updateSubtitle(getString(R.string.city_selection_title))
                    forecastViewModel.gotoCitySelection()
                }
            ).show()
            alertDialog.setCancelable(false)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.closeWith(this)
        }
    }

    private fun showStatusDependingOnCity(it: String) {
        if (it.isBlank()) {
            appBarViewModel.updateSubtitle(getString(R.string.forecast_downloading))
        } else {
            appBarViewModel.updateSubtitle(
                getString(R.string.forecast_downloading_for_city_text, it)
            )
        }
    }

    private fun showNoPermissionAlertDialog() {
        appBarViewModel.updateSubtitle(getString(R.string.geo_location_permission_required))
        mainView?.run {
            val alertDialog = dialogHelper.getLocationPermissionAlertDialogBuilder(
                onPositiveClick = {
                    geoLocationViewModel.requestGeoLocationPermission()
                },
                onNegativeClick = {
                    activity?.finish()
                }
            ).show()
            alertDialog.setCancelable(false)
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.closeWith(this)
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showToastAndOpenAppSettings() {
        val intent = Intent(
            ACTION_APPLICATION_DETAILS_SETTINGS,
            ("package:" + activity?.packageName).toUri()
        )
        startActivity(intent)
        // TODO replace with alert dialog
        showToast(getString(R.string.geo_location_permission_denied))
        showToast(getString(R.string.geo_location_permission_denied))
        showToast(getString(R.string.geo_location_permission_denied))
        exitProcess(0)
    }

    private fun showToast(toastMessage: String) {
        Toast.makeText(
            requireContext(),
            toastMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun gotoCitySelectionScreen() {
        findNavController().navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
    }
}