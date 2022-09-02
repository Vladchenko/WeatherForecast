package com.example.weatherforecast.presentation.fragments.forecast

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weatherforecast.R
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.util.WeatherForecastUtils.getCurrentDate
import com.example.weatherforecast.databinding.FragmentCurrentTimeForecastBinding
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.network.NetworkMonitor
import com.example.weatherforecast.presentation.PresentationUtils.animateFadeOut
import com.example.weatherforecast.presentation.PresentationUtils.getWeatherTypeIcon
import com.example.weatherforecast.presentation.PresentationUtils.setToolbarSubtitleFontSize
import com.example.weatherforecast.presentation.alertdialog.*
import com.example.weatherforecast.presentation.fragments.cityselection.CityClickListener
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.network.NetworkConnectionViewModel
import com.example.weatherforecast.presentation.viewmodel.persistence.PersistenceViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess


/**
 * Fragment representing a weather forecast for current time.
 */
@AndroidEntryPoint
class CurrentTimeForecastFragment : Fragment() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted ->
            forecastViewModel.onPermissionResolution(isGranted, chosenCity, savedCity)
        }

    private var savedCity: String = ""
    private var chosenCity: String = ""
    private lateinit var geoLocator: WeatherForecastGeoLocator
    private lateinit var fragmentDataBinding: FragmentCurrentTimeForecastBinding

    private val arguments: CurrentTimeForecastFragmentArgs by navArgs()
    private val forecastViewModel by activityViewModels<WeatherForecastViewModel>()
    private val persistenceViewModel by activityViewModels<PersistenceViewModel>()
    private val networkConnectionViewModel by activityViewModels<NetworkConnectionViewModel>()
    private var geoLocationAlertDialogDelegate: GeoLocationAlertDialogDelegate? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_current_time_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chosenCity = arguments.chosenCity
        forecastViewModel.setChosenCity(chosenCity)
        forecastViewModel.setTemperatureType(TemperatureType.CELSIUS)

        fragmentDataBinding = FragmentCurrentTimeForecastBinding.bind(view)
        geoLocator =
            WeatherForecastGeoLocator(forecastViewModel)   //TODO Is this instantiating correct ?
        NetworkMonitor(requireContext(), forecastViewModel)   //TODO Is this instantiating correct ?

        initViews()
        initLiveDataObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        geoLocationAlertDialogDelegate?.dismissAlertDialog()
    }

    private fun initViews() {
        fragmentDataBinding.cityNameTextView.setOnClickListener(
            CityClickListener(findNavController())
        )
        fragmentDataBinding.toolbar.title = getString(R.string.app_name)
        toggleProgressBar(true)
    }

    private fun initLiveDataObservers() {
        persistenceViewModel.onCityDownloadedLiveData.observe(viewLifecycleOwner) {
            savedCity = it.city
        }
        forecastViewModel.onGetWeatherForecastLiveData.observe(viewLifecycleOwner) {
            showAndSaveForecastData(
                it
            )
        }
        forecastViewModel.onShowErrorLiveData.observe(viewLifecycleOwner) { showError(it) }
        forecastViewModel.onUpdateStatusLiveData.observe(viewLifecycleOwner) { showStatus(it) }
        forecastViewModel.onShowProgressBarLiveData.observe(viewLifecycleOwner) {
            toggleProgressBar(
                it
            )
        }
        forecastViewModel.onShowGeoLocationAlertDialogLiveData.observe(viewLifecycleOwner) {
            showGeoLocationAlertDialog(
                it
            )
        }
        forecastViewModel.onShowLocationPermissionAlertDialogLiveData.observe(viewLifecycleOwner) {
            showLocationPermissionAlertDialog()
        }
        forecastViewModel.onDefineCityByGeoLocationLiveData.observe(viewLifecycleOwner) {
            defineCityByLatLong(
                it
            )
        }
        forecastViewModel.onDefineCityByCurrentGeoLocationLiveData.observe(viewLifecycleOwner) {
            defineCityByCurrentLocation(
                it
            )
        }
//        forecastViewModel.onLoadCityFromDatabaseLiveData.observe(viewLifecycleOwner) {
//            checkCityInDataBase()
//        }
        forecastViewModel.onRequestPermissionLiveData.observe(viewLifecycleOwner) { requestLocationPermission() }
        forecastViewModel.onRequestPermissionDeniedLiveData.observe(viewLifecycleOwner) { showToastAndOpenAppSettings() }
        forecastViewModel.onDefineCurrentGeoLocationLiveData.observe(viewLifecycleOwner) { defineCurrentGeoLocation() }
        forecastViewModel.onCityRequestFailedLiveData.observe(viewLifecycleOwner) {
            defineLocationByCity(
                it
            )
        }
        forecastViewModel.onGotoCitySelectionLiveData.observe(viewLifecycleOwner) { gotoCitySelection() }
        forecastViewModel.onChosenCityNotFoundLiveData.observe(viewLifecycleOwner) {
            showAlertDialogToChooseAnotherCity(
                it
            )
        }
    }

    private fun showAndSaveForecastData(dataModel: WeatherForecastDomainModel) {
        showForecastData(dataModel)
        persistenceViewModel.saveForecastToDatabase(dataModel)
        persistenceViewModel.saveChosenCity(dataModel)
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

    private fun defineCurrentGeoLocation() {
        Log.d("CurrentTimeForecastFragment", "Locating city...")
        geoLocator.defineCurrentLocation(requireActivity())
    }

    private fun gotoCitySelection() {
        findNavController().navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
    }

    private fun showAlertDialogToChooseAnotherCity(city: String) {
        CitySelectionAlertDialogDelegate(
            city,
            CityWrongAlertDialogListenerImpl(forecastViewModel, requireContext())
        ).showAlertDialog(requireContext())
    }

    private fun defineLocationByCity(city: String) {
        val geoCoder = Geocoder(activity as Context, Locale.getDefault())
        Log.d("CurrentTimeForecastFragment", "city = $city")
        val address: Address?
        try {
            address = geoCoder.getFromLocationName(city, 1).first()
            forecastViewModel.onDefineGeoLocationByCitySuccess(city, getLocationByAddress(address))
        } catch (ioex: IOException) {
            forecastViewModel.onDefineGeoLocationByCityFail(ioex.message.toString())
        }
    }

    private fun getLocationByAddress(address: Address): Location {
        val location = Location(LocationManager.NETWORK_PROVIDER)
        location.latitude = address.latitude
        location.longitude = address.longitude
        return location
    }

    private fun defineCityByLatLong(location: Location) {
        val geoCoder = Geocoder(activity as Context, Locale.getDefault())
        val locality =
            geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
        Log.d("CurrentTimeForecastFragment", "City for $location is defined as $locality")
        forecastViewModel.onDefineCityByGeoLocationSuccess(locality, location)
    }

    private fun defineCityByCurrentLocation(location: Location) {
        val geoCoder = Geocoder(activity as Context, Locale.getDefault())
        val locality =
            geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
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

    private fun showGeoLocationAlertDialog(city: String) {
        Log.d("CurrentTimeForecastFragment", "Geo location AlertDialog shown")
        geoLocationAlertDialogDelegate = GeoLocationAlertDialogDelegate(
            city,
            CityApprovalAlertDialogListenerImpl(forecastViewModel, requireContext())
        )
        geoLocationAlertDialogDelegate?.showAlertDialog(requireContext())
    }

    private fun showLocationPermissionAlertDialog() {
        Log.d("CurrentTimeForecastFragment", "Permission not granted AlertDialog shown")
        val locationPermissionAlertDialogDelegate = LocationPermissionAlertDialogDelegate(
            LocationPermissionAlertDialogListenerImpl(forecastViewModel, requireContext())
        )
        locationPermissionAlertDialogDelegate.showAlertDialog(requireContext())
    }

//    private fun checkCityInDataBase() {
//        persistenceViewModel.loadChosenCity()
//    }
}