package com.example.weatherforecast.presentation.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.data.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.databinding.FragmentCurrentTimeForecastBinding
import com.example.weatherforecast.geolocation.AlertDialogClickListener
import com.example.weatherforecast.geolocation.AlertDialogDelegate
import com.example.weatherforecast.geolocation.GeoLocationListener
import com.example.weatherforecast.geolocation.GeoLocationPermissionDelegate
import com.example.weatherforecast.geolocation.GeoLocationPermissionDelegate.Companion.REQUEST_CODE_ASK_PERMISSIONS
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.network.NetworkConnectionLiveData
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable
import com.example.weatherforecast.presentation.WeatherForecastActivity
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

/**
 * Fragment representing a weather forecast for current time.
 */
@AndroidEntryPoint
class CurrentTimeForecastFragment : Fragment() {

    private var city: String = ""
    private var temperatureType: TemperatureType = TemperatureType.CELSIUS
    private var localLocation: Location? = Location("")

    private lateinit var viewModel: WeatherForecastViewModel
    private lateinit var locationListener: GeoLocationListener
    private lateinit var fragmentDataBinding: FragmentCurrentTimeForecastBinding

    @Inject
    lateinit var geoLocator: WeatherForecastGeoLocator

    @Inject
    lateinit var mNetworkConnectionLiveData: NetworkConnectionLiveData

    @Inject
    lateinit var permissionDelegate: GeoLocationPermissionDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            city = it.getString(CITY_ARGUMENT_KEY) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_current_time_forecast, container, false)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i("CurrentTimeForecastFragment", "Permission Granted!")
                downloadWeatherForecastData(
                    TemperatureType.CELSIUS,
                    city,
                    localLocation
                )
            } else {
                Log.i("CurrentTimeForecastFragment", "Permission Not Granted!")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentDataBinding = FragmentCurrentTimeForecastBinding.bind(view)
        locationListener = GeoLocationListenerImpl()
        permissionDelegate.getPermissionForGeoLocation(activity as Activity)
        fragmentDataBinding.cityNameTextView.setOnClickListener(
            CityClickListener(findNavController())
        )
        viewModel = (activity as WeatherForecastActivity).forecastViewModel
        // locateCityOrDownloadForeCastData()
        prepareObservers()
    }

    private fun prepareObservers() {
        viewModel.getWeatherForecastLiveData.observe(this) { showForecastData(it) }
        viewModel.showErrorLiveData.observe(this) { showError(it) }
        viewModel.showProgressBarLiveData.observe(this) { toggleProgressBar(it) }
        mNetworkConnectionLiveData.observe(this) {
            viewModel._isNetworkAvailable.value = it
            viewModel.notifyAboutNetworkAvailability { onNetworkAvailable() }
        }
    }

    private fun locateCityOrDownloadForeCastData() {
        if (city.isBlank()) {
            geoLocator.getCityByLocation(activity as Activity, locationListener)
        } else {
            downloadWeatherForecastData(
                TemperatureType.CELSIUS,
                city,
                localLocation
            )
        }
    }

    private fun downloadWeatherForecastData(temperatureType: TemperatureType, city: String, location: Location?) {
        this.city = city
        localLocation = location
        this.temperatureType = temperatureType
        fragmentDataBinding.errorTextView.text = ""
        fragmentDataBinding.errorTextView.visibility =View.INVISIBLE
        viewModel.getWeatherForecast(temperatureType, city, location)
    }

    private fun showForecastData(dataModel: WeatherForecastDomainModel) {
        fragmentDataBinding.dateTextView.text = getCurrentDate()
        fragmentDataBinding.cityNameTextView.text = dataModel.city
        fragmentDataBinding.degreesValueTextView.text = dataModel.temperature
        fragmentDataBinding.degreesTypeTextView.text = dataModel.temperatureType
        fragmentDataBinding.weatherTypeTextView.text = dataModel.weatherType
        fragmentDataBinding.weatherTypeImageView.setImageResource(getWeatherTypeIcon(dataModel.weatherType))
        animateFadeOut(fragmentDataBinding.progressBar, resources.getInteger(android.R.integer.config_mediumAnimTime))
    }

    private fun showError(errorMessage: String) {
        toggleProgressBar(false)
        Log.e("CurrentTimeForecastFragment", errorMessage)
        fragmentDataBinding.errorTextView.text = errorMessage
        fragmentDataBinding.errorTextView.visibility =View.VISIBLE
    }

    private fun onNetworkAvailable() {
        if (viewModel._isNetworkAvailable.value == true
            || isNetworkAvailable(context)
        ) {
            fragmentDataBinding.errorTextView.visibility =View.INVISIBLE
            toggleProgressBar(true)
            locateCityOrDownloadForeCastData()
        } else {
            showError(getString(R.string.network_not_available))
        }
    }

    private fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateInstance()
        return formatter.format(date)
    }

    private fun getWeatherTypeIcon(weatherType: String) =
        resources.getIdentifier(
            ICON_PREFIX + weatherType.replace(" ", ""),
            DRAWABLE_RESOURCE_TYPE,
            requireActivity().packageName
        )

    private fun toggleProgressBar(toggle: Boolean) {
        if (toggle) {
            fragmentDataBinding.progressBar.visibility = View.VISIBLE
        } else {
            fragmentDataBinding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun animateFadeOut(progressBar: View, shortAnimationDuration: Int) {
        progressBar.apply {
            animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        progressBar.visibility = View.GONE
                    }
                })
        }
    }

    companion object {
        const val CITY_ARGUMENT_KEY = "CITY"
        private const val ICON_PREFIX = "icon_"
        private const val DRAWABLE_RESOURCE_TYPE = "drawable"
    }

    inner class GeoLocationListenerImpl : GeoLocationListener {
        override fun onGeoLocationSuccess(activity: Activity, location: Location, locationName: String) {
            city = locationName
            localLocation = location
            AlertDialogDelegate(city, CityApprovalAlertDialogListenerImpl()).showAlertDialog(activity)
        }
    }

    inner class CityApprovalAlertDialogListenerImpl : AlertDialogClickListener {
        override fun onPositiveClick(locationName: String) {
            if (permissionDelegate.getPermissionForGeoLocation(activity as Activity)
                == GeoLocationPermissionDelegate.LocationPermission.ALREADY_PRESENT
            ) {
                downloadWeatherForecastData(
                    TemperatureType.CELSIUS,
                    locationName,
                    localLocation
                )
            }
        }

        override fun onNegativeClick() {
            findNavController().navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
        }
    }
}