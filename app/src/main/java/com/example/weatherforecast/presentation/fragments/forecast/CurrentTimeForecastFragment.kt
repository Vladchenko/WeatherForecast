package com.example.weatherforecast.presentation.fragments.forecast

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weatherforecast.R
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.util.WeatherForecastUtils.getCurrentDate
import com.example.weatherforecast.databinding.FragmentCurrentTimeForecastBinding
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.presentation.PresentationUtils.animateFadeOut
import com.example.weatherforecast.presentation.PresentationUtils.getWeatherTypeIcon
import com.example.weatherforecast.presentation.PresentationUtils.setToolbarSubtitleFontSize
import com.example.weatherforecast.presentation.fragments.cityselection.CityClickListener
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

/**
 * Fragment representing a weather forecast for current time.
 */
@AndroidEntryPoint
class CurrentTimeForecastFragment : Fragment(R.layout.fragment_current_time_forecast) {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted ->
            forecastViewModel.onPermissionResolution(isGranted, chosenCity)
        }

    private val dialogHelper by lazy { AlertDialogHelper(requireContext()) }
    private val geolocationHelper by lazy { GeolocationHelper(requireContext()) }

    private var chosenCity: String = ""
    private lateinit var fragmentDataBinding: FragmentCurrentTimeForecastBinding

    private val arguments: CurrentTimeForecastFragmentArgs by navArgs()
    private val forecastViewModel by activityViewModels<WeatherForecastViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chosenCity = arguments.chosenCity
        forecastViewModel.setChosenCity(chosenCity)
        forecastViewModel.loadSavedCityAndRunNetworkMonitor()   // TODO Ask about it
        forecastViewModel.setTemperatureType(TemperatureType.CELSIUS)

        fragmentDataBinding = FragmentCurrentTimeForecastBinding.bind(view)
        initViews()
        initLiveDataObservers()
    }

    private fun initViews() {
        fragmentDataBinding.cityNameTextView.setOnClickListener(
            CityClickListener(findNavController())
        )
        fragmentDataBinding.toolbar.title = getString(R.string.app_name)
        toggleProgressBar(true)
    }

    private fun initLiveDataObservers() {
        forecastViewModel.onChosenCityNotFoundLiveData.observe(viewLifecycleOwner) {
            dialogHelper.showAlertDialogToChooseAnotherCity(
                it,
                onPositiveClick = { forecastViewModel.onGotoCitySelection() },
                onNegativeClick = {/*pass*/ }
            )
        }
        forecastViewModel.onCityRequestFailedLiveData.observe(viewLifecycleOwner) {
            defineLocationByCity(it)
        }
        forecastViewModel.onDefineCityByCurrentGeoLocationLiveData.observe(viewLifecycleOwner) {
            defineCityByCurrentLocation(it)
        }
        forecastViewModel.onGetWeatherForecastLiveData.observe(viewLifecycleOwner) {
            showForecastData(it)
        }
        forecastViewModel.onGotoCitySelectionLiveData.observe(viewLifecycleOwner) { gotoCitySelection() }
        forecastViewModel.onRequestPermissionLiveData.observe(viewLifecycleOwner) {
            requestLocationPermission()
        }
        forecastViewModel.onRequestPermissionDeniedLiveData.observe(viewLifecycleOwner) {
            showToastAndOpenAppSettings()
        }
        forecastViewModel.onShowErrorLiveData.observe(viewLifecycleOwner) { showError(it) }
        forecastViewModel.onShowGeoLocationAlertDialogLiveData.observe(viewLifecycleOwner) {
            dialogHelper.showGeoLocationAlertDialog(
                it,
                onPositiveClick = {
                    forecastViewModel.onUpdateStatus(
                        getString(
                            R.string.forecast_downloading_for_city_text,
                            it
                        )
                    )
                    forecastViewModel.downloadWeatherForecastForCityOrGeoLocation(it)
                },
                onNegativeClick = {
                    forecastViewModel.onGotoCitySelection()
                }
            )
        }
        forecastViewModel.onShowLocationPermissionAlertDialogLiveData.observe(viewLifecycleOwner) {
            dialogHelper.showLocationPermissionAlertDialog(
                onPositiveClick = {
                    forecastViewModel.onUpdateStatus(getString(R.string.geo_location_permission_required))
                    forecastViewModel.requestGeoLocationPermissionOrLoadForecast()
                },
                onNegativeClick = {
                    activity?.finish()
                }
            )
        }
        forecastViewModel.onShowProgressBarLiveData.observe(viewLifecycleOwner) {
            toggleProgressBar(it)
        }
        forecastViewModel.onUpdateStatusLiveData.observe(viewLifecycleOwner) { showStatus(it) }
    }

    private fun showForecastData(dataModel: WeatherForecastDomainModel) {
        showStatus(getString(R.string.forecast_for_city, dataModel.city))
        fragmentDataBinding.dateTextView.text =
            getCurrentDate(dataModel.date, getString(R.string.bad_date_format))
        fragmentDataBinding.cityNameTextView.text = dataModel.city
        fragmentDataBinding.degreesValueTextView.text = dataModel.temperature
        fragmentDataBinding.degreesTypeTextView.text = dataModel.temperatureType
        fragmentDataBinding.weatherTypeTextView.text = dataModel.weatherType
        fragmentDataBinding.weatherTypeImageView.visibility = View.VISIBLE
        fragmentDataBinding.weatherTypeImageView.setImageResource(
            getWeatherTypeIcon(resources, requireActivity().packageName, dataModel.weatherType)
        )
        toggleProgressBar(false)
    }

    private fun showError(errorMessage: String) {
        Log.e("CurrentTimeForecastFragment", errorMessage)
        toggleProgressBar(false)
        setToolbarSubtitleFontSize(fragmentDataBinding.toolbar, errorMessage)
        fragmentDataBinding.toolbar.subtitle = errorMessage
        fragmentDataBinding.toolbar.setBackgroundColor((activity as Context).getColor(R.color.colorAccent))
    }

    private fun showStatus(statusMessage: String) {
        Log.d("CurrentTimeForecastFragment", statusMessage)
        setToolbarSubtitleFontSize(fragmentDataBinding.toolbar, statusMessage)
        fragmentDataBinding.toolbar.subtitle = statusMessage
        fragmentDataBinding.toolbar.setBackgroundColor((activity as Context).getColor(R.color.colorPrimary))
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

    private fun gotoCitySelection() {
        findNavController().navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
    }

    private fun defineLocationByCity(city: String) = lifecycleScope.launchWhenCreated {
        try {
            val location = geolocationHelper.defineLocationByCity(city)
            Log.d("CurrentTimeForecastFragment", "Location for city = $city, is $location")
            forecastViewModel.onDefineGeoLocationByCitySuccess(city, location)
        } catch (nsee: NoSuchElementException) {
            showError("Forecast for city $city is not available")
        }
    }

    private fun defineCityByCurrentLocation(location: Location) = lifecycleScope.launchWhenCreated {
        showStatus("Defining city from geo location")
        val locality = geolocationHelper.loadCityByLocation(location)
        Log.d("CurrentTimeForecastFragment", "City for current location defined as $locality")
        forecastViewModel.onDefineCityByCurrentGeoLocationSuccess(locality)
    }

    private fun toggleProgressBar(isVisible: Boolean) {
        if (isVisible) {
            fragmentDataBinding.progressBar.alpha =
                resources.getFloat(R.dimen.progressbar_background_transparency)
            fragmentDataBinding.progressBar.visibility = View.VISIBLE
        } else {
            animateFadeOut(
                fragmentDataBinding.progressBar,
                resources.getInteger(android.R.integer.config_mediumAnimTime)
            )
        }
    }
}