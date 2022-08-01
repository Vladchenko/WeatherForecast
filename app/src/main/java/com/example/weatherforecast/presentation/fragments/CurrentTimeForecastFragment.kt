package com.example.weatherforecast.presentation.fragments

import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    private lateinit var viewModel: WeatherForecastViewModel
    private lateinit var locationListener: GeoLocationListener
    private lateinit var fragmentDataBinding: FragmentCurrentTimeForecastBinding

    @Inject
    lateinit var geoLocator: WeatherForecastGeoLocator

    @Inject
    lateinit var permissionDelegate: GeoLocationPermissionDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            city = it.getString(CITY_ARGUMENT_KEY) ?:""
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
        viewModel = (activity as WeatherForecastActivity).forecastViewModel
        locationListener = GeoLocationListenerImpl()
        permissionDelegate.getPermissionForGeoLocation(activity as Activity)
        fragmentDataBinding.cityNameTextView.setOnClickListener(
            CityClickListener(findNavController())
        )

        if (city.isBlank()) {
            geoLocator.getCityByLocation(activity as Activity, locationListener)
        } else {
            viewWeatherForecastData(
                TemperatureType.CELSIUS,
                city,
                Location("")
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i("CurrentTimeForecastFragment", "Permission Granted!")
                viewWeatherForecastData(
                    TemperatureType.CELSIUS,
                    city,
                    Location("")
                )
            } else {
                Log.i("CurrentTimeForecastFragment", "Permission Not Granted!")
            }
        }
    }

    private fun viewWeatherForecastData(temperatureType: TemperatureType, city: String, location: Location?) {
        viewModel.getWeatherForecast(temperatureType, city, location)
        viewModel.getWeatherForecastLiveData.observe(this) { showForecastData(it) }
        viewModel.showErrorLiveData.observe(this) { showError(it) }
    }

    private fun showForecastData(dataModel: WeatherForecastDomainModel) {
        hideProgressBar()
        fragmentDataBinding.dateTextView.text = getCurrentDate()
        fragmentDataBinding.cityNameTextView.text = dataModel.city
        fragmentDataBinding.degreesValueTextView.text = dataModel.temperature
        fragmentDataBinding.degreesTypeTextView.text = dataModel.temperatureType
        fragmentDataBinding.weatherTypeTextView.text = dataModel.weatherType
        fragmentDataBinding.weatherTypeImageView.setImageResource(getWeatherTypeIcon(dataModel.weatherType))
    }

    private fun showError(errorMessage: String) {
        hideProgressBar()
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
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

    private fun hideProgressBar() {
        fragmentDataBinding.progressBar.visibility = View.INVISIBLE
    }

    companion object {
        const val CITY_ARGUMENT_KEY = "CITY"
        private const val ICON_PREFIX = "icon_"
        private const val DRAWABLE_RESOURCE_TYPE = "drawable"
    }

    inner class GeoLocationListenerImpl: GeoLocationListener {
        override fun onGeoLocationSuccess(activity: Activity, location: Location, locationName: String) {
            city = locationName
            AlertDialogDelegate(city, CityApprovalAlertDialogListenerImpl()).showAlertDialog(activity)
        }
    }

    inner class CityApprovalAlertDialogListenerImpl: AlertDialogClickListener {
        override fun onPositiveClick(locationName: String) {
            fragmentDataBinding.progressBar.visibility = View.VISIBLE
            if (permissionDelegate.getPermissionForGeoLocation(activity as Activity)
                == GeoLocationPermissionDelegate.LocationPermission.ALREADY_PRESENT
            ) {
                viewWeatherForecastData(
                    TemperatureType.CELSIUS,
                    locationName,
                    Location("")    //FIXME
                )
            }
        }

        override fun onNegativeClick() {
            findNavController().navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
        }
    }
}