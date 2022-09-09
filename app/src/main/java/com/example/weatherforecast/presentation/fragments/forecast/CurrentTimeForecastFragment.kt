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
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModel
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
            geoLocationViewModel.onPermissionResolution(isGranted, chosenCity, savedCity)
        }

    private var savedCity: String = ""
    private var chosenCity: String = ""
    private lateinit var geoLocator: WeatherForecastGeoLocator
    private lateinit var fragmentDataBinding: FragmentCurrentTimeForecastBinding

    private val arguments: CurrentTimeForecastFragmentArgs by navArgs()
    private val persistenceViewModel by activityViewModels<PersistenceViewModel>()
    private val geoLocationViewModel by activityViewModels<GeoLocationViewModel>()
    private val forecastViewModel by activityViewModels<WeatherForecastViewModel>()
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
        persistenceViewModel.loadSavedCity()
        forecastViewModel.setTemperatureType(TemperatureType.CELSIUS)

        fragmentDataBinding = FragmentCurrentTimeForecastBinding.bind(view)
        //TODO Is this instantiating correct ?
        geoLocator = WeatherForecastGeoLocator(geoLocationViewModel)
        //TODO Is this instantiating correct ?
        NetworkMonitor(requireContext(), networkConnectionViewModel)

        initViews()
        initLiveDataObservers()
        // Since ConnectivityManager.NetworkCallback checks only network changes during an app
        // operating (it doesn't check if connection is already absent at app launch), thus,
        // one needs to add a following check for connection availability.
        forecastViewModel.onNetworkNotAvailable(
            geoLocationViewModel.hasPermissionForGeoLocation(),
            savedCity
        )
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
        forecastViewModel.onDownloadLocalForecastLiveData.observe(viewLifecycleOwner) {
            persistenceViewModel.downloadLocalForecast(it)
        }
        forecastViewModel.onGetWeatherForecastLiveData.observe(viewLifecycleOwner) {
            showForecastData(it)
            persistenceViewModel.saveForecastToDatabase(it)
            persistenceViewModel.saveChosenCity(it)
        }
        forecastViewModel.onShowErrorLiveData.observe(viewLifecycleOwner) { showError(it) }
        forecastViewModel.onUpdateStatusLiveData.observe(viewLifecycleOwner) { showStatus(it) }
        forecastViewModel.onShowProgressBarLiveData.observe(viewLifecycleOwner) {
            toggleProgressBar(
                it
            )
        }
        forecastViewModel.onCityRequestFailedLiveData.observe(viewLifecycleOwner) {
            defineLocationByCity(
                it
            )
        }
        forecastViewModel.onDefineCurrentGeoLocationLiveData.observe(viewLifecycleOwner) {
            showStatus(getString(R.string.current_location_defining_text))
            defineCurrentGeoLocation()
        }
        forecastViewModel.onGotoCitySelectionLiveData.observe(viewLifecycleOwner) { gotoCitySelection() }
        forecastViewModel.onChosenCityNotFoundLiveData.observe(viewLifecycleOwner) {
            showAlertDialogToChooseAnotherCity(
                it
            )
        }

        geoLocationViewModel.onShowGeoLocationAlertDialogLiveData.observe(viewLifecycleOwner) {
            showGeoLocationAlertDialog(
                it
            )
        }
        geoLocationViewModel.onShowLocationPermissionAlertDialogLiveData.observe(viewLifecycleOwner) {
            showLocationPermissionAlertDialog()
        }
        geoLocationViewModel.onDefineCurrentGeoLocationLiveData.observe(viewLifecycleOwner) {
            showStatus(getString(R.string.current_location_defining_text))
            defineCurrentGeoLocation()
        }
        geoLocationViewModel.onDefineCityByCurrentGeoLocationLiveData.observe(viewLifecycleOwner) {
            defineCityByCurrentLocation(it)
        }
        geoLocationViewModel.onDefineCityByGeoLocationLiveData.observe(viewLifecycleOwner) {
            defineCityByGeoLocation(
                it
            )
        }
        geoLocationViewModel.onRequestPermissionLiveData.observe(viewLifecycleOwner) {
            requestLocationPermission()
        }
        geoLocationViewModel.onRequestPermissionDeniedLiveData.observe(viewLifecycleOwner) {
            showToastAndOpenAppSettings()
        }
        geoLocationViewModel.onGetWeatherForecastForCityLiveData.observe(viewLifecycleOwner) {
            forecastViewModel.getWeatherForecastForCity(it)
        }
        geoLocationViewModel.onGetWeatherForecastForLocationLiveData.observe(viewLifecycleOwner) {
            forecastViewModel.getWeatherForecastForLocation(it)
        }
        geoLocationViewModel.onSaveCityLiveData.observe(viewLifecycleOwner) {
            persistenceViewModel.saveChosenCity(it)
        }
        geoLocationViewModel.onShowErrorLiveData.observe(viewLifecycleOwner) { showError(it) }
        geoLocationViewModel.onShowProgressBarLiveData.observe(viewLifecycleOwner) {
            toggleProgressBar(
                it
            )
        }

        networkConnectionViewModel.onNetworkConnectionAvailableLiveData.observe(viewLifecycleOwner) {
            geoLocationViewModel.requestGeoLocationPermissionOrDownloadWeatherForecast(
                chosenCity,
                savedCity
            )
        }
        networkConnectionViewModel.onNetworkConnectionLostLiveData.observe(viewLifecycleOwner) {
            forecastViewModel.getWeatherForecastForCity("")
        }
        networkConnectionViewModel.onShowErrorLiveData.observe(viewLifecycleOwner) { showError(it) }

        persistenceViewModel.onCityDownloadedLiveData.observe(viewLifecycleOwner) {
            savedCity = it.city
            downloadWeatherForecast(chosenCity, savedCity)
        }
        persistenceViewModel.onForecastDownloadedLiveData.observe(viewLifecycleOwner) {
            showForecastData(it)
        }
    }

    private fun downloadWeatherForecast(chosenCity: String, savedCity: String) {
        if (chosenCity.isNotBlank()) {
            forecastViewModel.getWeatherForecastForCity(chosenCity)
        } else {
            forecastViewModel.getWeatherForecastForCity(savedCity)
        }
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
            geoLocationViewModel.onDefineGeoLocationByCitySuccess(
                city,
                getLocationByAddress(address)
            )
        } catch (ioex: IOException) {
            geoLocationViewModel.onDefineGeoLocationByCityFail(ioex.message.toString())
        }
    }

    private fun getLocationByAddress(address: Address): Location {
        val location = Location(LocationManager.NETWORK_PROVIDER)
        location.latitude = address.latitude
        location.longitude = address.longitude
        return location
    }

    private fun defineCityByGeoLocation(location: Location) {
        val locality = getCityByLocation(location)
        Log.d("CurrentTimeForecastFragment", "City for $location is defined as $locality")
        geoLocationViewModel.onDefineCityByGeoLocationSuccess(locality, location)
    }

    private fun defineCityByCurrentLocation(location: Location) {
        val locality = getCityByLocation(location)
        Log.d("CurrentTimeForecastFragment", "City for current location defined as $locality")
        geoLocationViewModel.onDefineCityByCurrentGeoLocationSuccess(locality)
    }

    private fun getCityByLocation(location: Location): String {
        val geoCoder = Geocoder(activity as Context, Locale.getDefault())
        showStatus("Defining city from geo location")
        toggleProgressBar(true)
        val city = getLocation(geoCoder, location)
        toggleProgressBar(false)
        return city
    }

    private fun getLocation(geoCoder: Geocoder, location: Location):String {
        var city = ""
        try {
            city = geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
        } catch (ex: Exception) {
            Log.d("CurrentTimeForecastFragment",ex.message.toString())
            showError(ex.message.toString())
            getLocation(geoCoder, location)
        }
        return city
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
            LocationPermissionAlertDialogListenerImpl(geoLocationViewModel, requireContext())
        )
        locationPermissionAlertDialogDelegate.showAlertDialog(requireContext())
    }

//    private fun checkCityInDataBase() {
//        persistenceViewModel.loadChosenCity()
//    }
}