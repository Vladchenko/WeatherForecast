package com.example.weatherforecast.presentation.fragments

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.data.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.util.WeatherForecastUtils.getCurrentDate
import com.example.weatherforecast.databinding.FragmentCurrentTimeForecastBinding
import com.example.weatherforecast.geolocation.AlertDialogClickListener
import com.example.weatherforecast.geolocation.AlertDialogDelegate
import com.example.weatherforecast.geolocation.GeoLocationListener
import com.example.weatherforecast.geolocation.GeoLocationPermissionDelegate
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.network.NetworkConnectionLiveData
import com.example.weatherforecast.presentation.fragments.PresentationUtils.getWeatherTypeIcon
import com.example.weatherforecast.presentation.fragments.PresentationUtils.setToolbarSubtitleFontSize
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * Fragment representing a weather forecast for current time.
 */
@AndroidEntryPoint
class CurrentTimeForecastFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.locateCityOrDownloadForecastData()
        } else {
            exitProcess(0)  //TODO Inform user that app cannot proceed
        }
    }

    private val viewModel by activityViewModels<WeatherForecastViewModel>()

    private var localLocation: Location? = Location("")
    private var alertDialogDelegate: AlertDialogDelegate? = null
    private var temperatureType: TemperatureType = TemperatureType.CELSIUS

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fragmentDataBinding: FragmentCurrentTimeForecastBinding

    @Inject
    lateinit var geoLocator: WeatherForecastGeoLocator

    @Inject
    lateinit var networkConnectionLiveData: NetworkConnectionLiveData

    @Inject
    lateinit var permissionDelegate: GeoLocationPermissionDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = (activity as Activity).getPreferences(MODE_PRIVATE)
        arguments?.let {
            val city = it.getString(CITY_ARGUMENT_KEY) ?: ""
            Log.d("CurrentTimeForecastFragment", "city = $city")
            if (city.isNotBlank()) {
                sharedPreferences.edit().putString(CITY_ARGUMENT_KEY, city).apply()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_current_time_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentDataBinding = FragmentCurrentTimeForecastBinding.bind(view)
        fragmentDataBinding.cityNameTextView.setOnClickListener(
            CityClickListener(findNavController())
        )
        fragmentDataBinding.toolbar.title = getString(R.string.app_name)
        showStatus(getString(R.string.location_defining_text))
        toggleProgressBar(true)
        initLiveDataObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        alertDialogDelegate?.dismissAlertDialog()
    }

    private fun initLiveDataObservers() {
        viewModel.getWeatherForecastLiveData.observe(viewLifecycleOwner) { showForecastData(it) }
        viewModel.showErrorLiveData.observe(viewLifecycleOwner) { showError(it) }
        viewModel.showStatusLiveData.observe(viewLifecycleOwner) { showStatus(it) }
        viewModel.showProgressBarLiveData.observe(viewLifecycleOwner) { toggleProgressBar(it) }
        viewModel.showAlertDialogLiveData.observe(viewLifecycleOwner) { showAlertDialog() }
        viewModel.requestPermissionLiveData.observe(viewLifecycleOwner) { requestLocationPermission() }
        viewModel.locateCityLiveData.observe(viewLifecycleOwner) { locateCity() }
        networkConnectionLiveData.observe(viewLifecycleOwner) {
            viewModel.isNetworkAvailableLiveData.value = it
            viewModel.notifyAboutNetworkAvailability { viewModel.onNetworkAvailable(it) }
        }
    }

    private fun downloadWeatherForecastData(temperatureType: TemperatureType, city: String, location: Location?) {
        localLocation = location
        this.temperatureType = temperatureType
        showStatus(getString(R.string.forecast_pending_text))
        viewModel.downloadWeatherForecast(temperatureType, city, location)
    }

    private fun showForecastData(dataModel: WeatherForecastDomainModel) {
        showStatus(getString(R.string.forecast_for_city, dataModel.city))
        fragmentDataBinding.dateTextView.text = getCurrentDate(dataModel.date, getString(R.string.bad_date_format))
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
        setToolbarSubtitleFontSize(fragmentDataBinding.toolbar, errorMessage)
        fragmentDataBinding.toolbar.subtitle = errorMessage
        fragmentDataBinding.toolbar.setBackgroundColor((activity as Context).getColor(R.color.colorAccent))
    }

    private fun showStatus(statusMessage: String) {
        Log.e("CurrentTimeForecastFragment", statusMessage)
        setToolbarSubtitleFontSize(fragmentDataBinding.toolbar, statusMessage)
        fragmentDataBinding.toolbar.subtitle = statusMessage
        fragmentDataBinding.toolbar.setBackgroundColor((activity as Context).getColor(R.color.colorPrimary))
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun locateCity() {
        geoLocator.getCityByLocation(WeakReference(activity as Activity), GeoLocationListenerImpl())
    }

    private fun toggleProgressBar(isVisible: Boolean) {
        if (isVisible) {
            fragmentDataBinding.progressBar.visibility = View.VISIBLE
        } else {
            animateFadeOut(
                fragmentDataBinding.progressBar,
                resources.getInteger(android.R.integer.config_mediumAnimTime)
            )
        }
    }

    private fun animateFadeOut(view: View, shortAnimationDuration: Int) {
        view.apply {
            animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                    }
                })
        }
    }

    private fun showAlertDialog() {
        alertDialogDelegate = AlertDialogDelegate(
            sharedPreferences.getString(CITY_ARGUMENT_KEY, "") ?: "",
            CityApprovalAlertDialogListenerImpl()
        )
        alertDialogDelegate?.showAlertDialog(WeakReference(activity as Activity))
    }

    companion object {
        const val CITY_ARGUMENT_KEY = "CITY"
    }

    inner class GeoLocationListenerImpl : GeoLocationListener {
        override fun onGeoLocationSuccess(
            location: Location,
            locationName: String
        ) {
            if (fragmentDataBinding.cityNameTextView.text.isNullOrBlank()) {
                sharedPreferences.edit().putString(CITY_ARGUMENT_KEY, locationName).apply()
                localLocation = location
                viewModel.showAlertDialog()
            }
        }

        override fun onGeoLocationFail() {
            showError(getString(R.string.location_fail_error_text))
        }
    }

    inner class CityApprovalAlertDialogListenerImpl : AlertDialogClickListener {
        override fun onPositiveClick(locationName: String) {
            downloadWeatherForecastData(
                TemperatureType.CELSIUS,
                locationName,
                localLocation
            )
        }

        override fun onNegativeClick() {
            findNavController().navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
        }
    }
}