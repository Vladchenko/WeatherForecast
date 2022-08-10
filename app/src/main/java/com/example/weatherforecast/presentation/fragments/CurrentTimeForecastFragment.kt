package com.example.weatherforecast.presentation.fragments

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import com.example.weatherforecast.databinding.FragmentCurrentTimeForecastBinding
import com.example.weatherforecast.geolocation.AlertDialogClickListener
import com.example.weatherforecast.geolocation.AlertDialogDelegate
import com.example.weatherforecast.geolocation.GeoLocationListener
import com.example.weatherforecast.geolocation.GeoLocationPermissionDelegate
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.network.NetworkConnectionLiveData
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
            locateCityOrDownloadForecastData()
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
        fragmentDataBinding.toolbar.subtitle = getString(R.string.location_defining_text)
        prepareObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        alertDialogDelegate?.dismissAlertDialog()
    }

    private fun prepareObservers() {
        viewModel.getWeatherForecastLiveData.observe(viewLifecycleOwner) { showForecastData(it) }
        viewModel.showErrorLiveData.observe(viewLifecycleOwner) { showError(it) }
        viewModel.showProgressBarLiveData.observe(viewLifecycleOwner) { toggleProgressBar(it) }
        viewModel.showAlertDialogLiveData.observe(viewLifecycleOwner) { showAlertDialog() }
        networkConnectionLiveData.observe(viewLifecycleOwner) {
            viewModel.isNetworkAvailableLiveData.value = it
            viewModel.notifyAboutNetworkAvailability { onNetworkAvailable(it) }
        }
    }

    // Put to viewmodel
    private fun locateCityOrDownloadForecastData() {
        val city = sharedPreferences.getString(CITY_ARGUMENT_KEY, "")
        Log.d("CurrentTimeForecastFragment", "city = $city")
        if (city.isNullOrBlank()) {
            fragmentDataBinding.toolbar.subtitle = getString(R.string.location_defining_text)
            geoLocator.getCityByLocation(WeakReference(activity as Activity), GeoLocationListenerImpl())
        } else {
            downloadWeatherForecastData(
                TemperatureType.CELSIUS,
                city,
                localLocation
            )
        }
    }

    private fun downloadWeatherForecastData(temperatureType: TemperatureType, city: String, location: Location?) {
        localLocation = location
        this.temperatureType = temperatureType
        fragmentDataBinding.toolbar.subtitle = getString(R.string.forecast_pending_text)
        // context.applicationInfo.theme
        fragmentDataBinding.toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary, resources.newTheme()))
        viewModel.downloadWeatherForecast(temperatureType, city, location)
    }

    private fun showForecastData(dataModel: WeatherForecastDomainModel) {
        fragmentDataBinding.toolbar.subtitle = "Provided for city ${dataModel.city}"
        fragmentDataBinding.toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        fragmentDataBinding.dateTextView.text = getCurrentDate(dataModel.date)
        fragmentDataBinding.cityNameTextView.text = dataModel.city
        fragmentDataBinding.degreesValueTextView.text = dataModel.temperature
        fragmentDataBinding.degreesTypeTextView.text = dataModel.temperatureType
        fragmentDataBinding.weatherTypeTextView.text = dataModel.weatherType
        fragmentDataBinding.weatherTypeImageView.visibility = View.VISIBLE
        fragmentDataBinding.weatherTypeImageView.setImageResource(getWeatherTypeIcon(dataModel.weatherType))
        animateFadeOut(fragmentDataBinding.progressBar, resources.getInteger(android.R.integer.config_mediumAnimTime))
    }

    private fun showError(errorMessage: String) {
        toggleProgressBar(false)
        Log.e("CurrentTimeForecastFragment", errorMessage)
        fragmentDataBinding.toolbar.subtitle = errorMessage
        fragmentDataBinding.toolbar.setBackgroundColor(resources.getColor(R.color.colorAccent))
    }

    // Put to view model
    private fun onNetworkAvailable(isAvailable: Boolean) {
        if (isAvailable) {
            fragmentDataBinding.toolbar.setBackgroundColor(resources.getColor(R.color.colorPrimary))
            toggleProgressBar(true)
            if (ActivityCompat.checkSelfPermission(activity as Context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                locateCityOrDownloadForecastData()
            }
        } else {
            sharedPreferences.edit().putString(CITY_ARGUMENT_KEY, "").apply()
            showError(getString(R.string.network_not_available_error_text))
        }
    }

    private fun getCurrentDate(time: String): String {
        return try {
            SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date(time.toLong() * 1000))
        } catch (ex: Exception) {
            getString(R.string.bad_date_format)
        }
    }

    private fun getWeatherTypeIcon(weatherType: String) =
        resources.getIdentifier(
            ICON_PREFIX + weatherType.replace(" ", ""),
            DRAWABLE_RESOURCE_TYPE,
            requireActivity().packageName
        )

    private fun toggleProgressBar(isVisible: Boolean) {
        if (isVisible) {
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

    private fun showAlertDialog() {
        alertDialogDelegate = AlertDialogDelegate(
            sharedPreferences.getString(CITY_ARGUMENT_KEY, "") ?: "",
            CityApprovalAlertDialogListenerImpl()
        )
        alertDialogDelegate?.showAlertDialog(WeakReference(activity as Activity))
    }

    companion object {
        const val CITY_ARGUMENT_KEY = "CITY"
        private const val ICON_PREFIX = "icon_"
        private const val DATE_FORMAT = "dd/MM/yyyy"
        private const val DRAWABLE_RESOURCE_TYPE = "drawable"
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
            fragmentDataBinding.toolbar.subtitle = getString(R.string.location_fail_error_text)
            fragmentDataBinding.toolbar.setBackgroundColor(resources.getColor(R.color.colorAccent))
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