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
import com.example.weatherforecast.presentation.PresentationUtils.closeWith
import com.example.weatherforecast.presentation.view.fragments.forecast.current.CurrentTimeForecastLayout
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
        forecastViewModel.launchWeatherForecast(arguments.chosenCity)

        (view as ComposeView).setContent {
            CurrentTimeForecastLayout(
                toolbarTitle = getString(R.string.app_name),
                currentDate = "",   // TODO Remove it
                mainContentTextColor = Color.Black,
                weatherImageId = 0, // TODO Remove it
                onCityClick = {
                    gotoCitySelectionScreen()
                },
                onBackClick = {
                    activity?.finish()
                },
                viewModel = forecastViewModel,
                hourlyViewModel = hourlyForecastViewModel
            )
        }
    }

    private fun initFlowObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    forecastViewModel.cityRequestFailedFlow.collect {
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
                        geoLocationViewModel.defineCurrentGeoLocation()
                    }
                }
                launch {
                    geoLocationViewModel.geoLocationByCitySuccessFlow.collect {
                        forecastViewModel.downloadRemoteForecastForLocation(it)
                    }
                }
                launch {
                    geoLocationViewModel.geoLocationSuccessFlow.collect {
                        geoLocationViewModel.defineCityNameByLocation(it)
                    }
                }
                launch {
                    geoLocationViewModel.geoGeoLocationPermissionFlow.collect {
                        when (it) {
                            GeoLocationPermission.Requested -> requestLocationPermission()
                            GeoLocationPermission.Denied -> showNoPermissionAlertDialog()
                            GeoLocationPermission.Granted -> geoLocationViewModel.defineCurrentGeoLocation()
                            GeoLocationPermission.PermanentlyDenied -> showToastAndOpenAppSettings()
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
            forecastViewModel.showStatus(
                getString(R.string.forecast_downloading)
            )
        } else {
            forecastViewModel.showStatus(
                getString(R.string.forecast_downloading_for_city_text, it)
            )
        }
    }

    private fun showNoPermissionAlertDialog() {
        mainView?.run {
            val alertDialog = dialogHelper.getLocationPermissionAlertDialogBuilder(
                onPositiveClick = {
                    geoLocationViewModel.showStatus(getString(R.string.geo_location_permission_required))
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