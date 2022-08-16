package com.example.weatherforecast.geolocation

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

/**
 * Defines location of a device.
 */
class WeatherForecastGeoLocator(private val viewModel: WeatherForecastViewModel) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Define device geo location, using [context].
     */
    fun getCityByLocation(context: Context) {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
                Log.d("WeatherForecastGeoLocator", location.toString())
                if (location == null) {
                    viewModel.onGeoLocationFail(context.getString(R.string.location_fail_error_text))
                } else {
                    Log.d("WeatherForecastGeoLocator", location.toString())
                    viewModel.onDefineCityByGeoLocation(location)
                }
            }.addOnFailureListener {
                Log.e("WeatherForecastGeoLocator",it.message.toString())
                viewModel.onShowError(it.message?:"")
            }.addOnCanceledListener {
                Log.e("WeatherForecastGeoLocator", "Cancelled")
                viewModel.onShowError(context.getString(R.string.city_locating_cancelled))
            }
        } catch (sec: SecurityException) {
            viewModel.requestLocationPermissionOrLocateCity()
        }
    }
}