package com.example.weatherforecast.presentation.fragments.forecast

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weatherforecast.R
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.util.WeatherForecastUtils.getCurrentDate
import com.example.weatherforecast.presentation.PresentationUtils.closeWith
import com.example.weatherforecast.presentation.PresentationUtils.getWeatherTypeIcon
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

/**
 * Fragment representing a weather forecast for current time.
 */
@AndroidEntryPoint
class CurrentTimeForecastFragment : Fragment() {

    private var mainView: View? = null
    private val arguments: CurrentTimeForecastFragmentArgs by navArgs()
    private val dialogHelper by lazy { AlertDialogHelper(requireContext()) }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted ->
            geoLocationViewModel.onPermissionResolution(isGranted)
        }
    private val forecastViewModel by activityViewModels<WeatherForecastViewModel>()
    private val geoLocationViewModel by activityViewModels<GeoLocationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        forecastViewModel.toolbarSubtitleState.value = getString(R.string.forecast_downloading_for_city_text)
        return ComposeView(requireContext()).apply {
            setContent {
                CurrentTimeForecastLayout(
                    toolbarTitle = getString(R.string.app_name),
                    currentDate = getCurrentDate(
                        forecastViewModel.dataModelState.value?.date.orEmpty(),
                        getString(R.string.bad_date_format)
                    ),
                    mainContentTextColor = Color.Black,
                    weatherImageId = getWeatherTypeIcon(
                        resources,
                        requireActivity().packageName,
                        forecastViewModel.dataModelState.value?.weatherType.orEmpty()
                    ),
                    onCityClick = {
                        gotoCitySelectionScreen()
                    },
                    onBackClick = {
                        activity?.finish()
                    },
                    viewModel = forecastViewModel
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainView = view
        super.onViewCreated(view, savedInstanceState)
        forecastViewModel.setTemperatureType(TemperatureType.CELSIUS)
        forecastViewModel.launchWeatherForecast(arguments.chosenCity)   // TODO Make a looped downloading
        initLiveDataObservers()
    }

    private fun initLiveDataObservers() {
        geoLocationViewModel.onLoadForecastLiveData.observe(viewLifecycleOwner) { city ->
            forecastViewModel.downloadWeatherForecastForCity(city)
        }
        forecastViewModel.onChosenCityNotFoundLiveData.observe(viewLifecycleOwner) {
            dialogHelper.getAlertDialogBuilderToChooseAnotherCity(
                it,
                onPositiveClick = { forecastViewModel.onGotoCitySelection() },
                onNegativeClick = {/*pass*/ }
            ).show().closeWith(mainView!!)
        }
        forecastViewModel.onCityRequestFailedLiveData.observe(viewLifecycleOwner) {
            geoLocationViewModel.defineLocationByCity(it)
        }
        forecastViewModel.onGotoCitySelectionLiveData.observe(viewLifecycleOwner) { gotoCitySelectionScreen() }
        forecastViewModel.onChosenCityBlankLiveData.observe(viewLifecycleOwner) {
            geoLocationViewModel.defineCurrentGeoLocation()
        }
        geoLocationViewModel.onDefineGeoLocationByCitySuccessLiveData.observe(viewLifecycleOwner) {
            forecastViewModel.downloadWeatherForecastForLocation(it)
        }
        geoLocationViewModel.onDefineCurrentGeoLocationSuccessLiveData.observe(viewLifecycleOwner) {
            geoLocationViewModel.defineCityNameByLocation(it)
        }
        geoLocationViewModel.onRequestPermissionLiveData.observe(viewLifecycleOwner) {
            requestLocationPermission()
        }
        geoLocationViewModel.onRequestPermissionDeniedLiveData.observe(viewLifecycleOwner) {
            showToastAndOpenAppSettings()
        }
        geoLocationViewModel.onShowGeoLocationAlertDialogLiveData.observe(viewLifecycleOwner) {
            locationDefinedAlertDialog(it)
        }
        geoLocationViewModel.onShowNoPermissionForLocationTriangulatingAlertDialogLiveData.observe(viewLifecycleOwner) {
            showNoPermissionAlertDialog()
        }
    }

    private fun locationDefinedAlertDialog(it: String) {
        dialogHelper.getGeoLocationAlertDialogBuilder(
            it,
            onPositiveClick = {
                forecastViewModel.onShowStatus(
                    R.string.forecast_downloading_for_city_text,
                    it
                )
                forecastViewModel.downloadWeatherForecastForCity(it)
            },
            onNegativeClick = {
                forecastViewModel.onGotoCitySelection()
            }
        ).show().closeWith(mainView!!)
    }

    private fun showNoPermissionAlertDialog() {
        dialogHelper.getLocationPermissionAlertDialogBuilder(
            onPositiveClick = {
                geoLocationViewModel.onShowStatus(getString(R.string.geo_location_permission_required))
                geoLocationViewModel.requestGeoLocationPermission()
            },
            onNegativeClick = {
                activity?.finish()
            }
        ).show().closeWith(mainView!!)
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showToastAndOpenAppSettings() {
        val intent = Intent(
            ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + activity?.packageName)
        )
        startActivity(intent)
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