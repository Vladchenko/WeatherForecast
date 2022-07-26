package com.example.weatherforecast.presentation

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.weatherforecast.R
import com.example.weatherforecast.data.models.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.databinding.FragmentCurrentTimeForecastBinding
import com.example.weatherforecast.geolocation.GeoLocationPermissionDelegate
import com.example.weatherforecast.geolocation.GeoLocationPermissionDelegate.Companion.REQUEST_CODE_ASK_PERMISSIONS
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

/**
 * Fragment displaying a weather forecast for current time.
 */
@AndroidEntryPoint
class CurrentTimeForecastFragment : Fragment() {

    //TODO Get city name from some place. Some dropdown list or from cellphone local area ?
    private var city: String = "Kazan"
    private var currentGeoLocation: Location? = null

    private lateinit var viewModel: WeatherForecastViewModel
    private lateinit var fragmentDataBinding: FragmentCurrentTimeForecastBinding

    @Inject
    lateinit var geoLocator: WeatherForecastGeoLocator
    @Inject
    lateinit var permissionDelegate: GeoLocationPermissionDelegate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_current_time_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentDataBinding = FragmentCurrentTimeForecastBinding.bind(view)
        viewModel = (activity as WeatherForecastActivity).viewModel
        if (permissionDelegate.getPermissionForGeoLocation(activity as Activity)
            == GeoLocationPermissionDelegate.LocationPermission.ALREADY_PRESENT
        ) {
            viewWeatherForecastData(
                TemperatureType.CELSIUS,
                city,
                currentGeoLocation
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Log.i("CurrentTimeForecastFragment", "Permission Granted!")
                viewWeatherForecastData(TemperatureType.CELSIUS, city, geoLocator.getLocation(activity as Activity))
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

    private companion object {
        private const val ICON_PREFIX = "icon_"
        private const val DRAWABLE_RESOURCE_TYPE = "drawable"
    }
}