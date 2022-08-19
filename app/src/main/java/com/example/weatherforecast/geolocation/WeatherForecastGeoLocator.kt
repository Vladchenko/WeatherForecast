package com.example.weatherforecast.geolocation

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.weatherforecast.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

/**
 * Defines location of a device.
 */
class WeatherForecastGeoLocator(private val locationListener: GeoLocationListener) {

    /**
     * Define current geo location for [chosenCity], using [context].
     */
    fun defineCurrentLocation(context: Context, chosenCity: String) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
                Log.d("WeatherForecastGeoLocator", location.toString())
                if (location == null) {
                    locationListener.onGeoLocationFail(context.getString(R.string.location_fail_error_text))
                } else {
                    Log.d("WeatherForecastGeoLocator", location.toString())
                    locationListener.onGeoLocationSuccess(location, chosenCity)
                }
            }.addOnFailureListener {
                Log.e("WeatherForecastGeoLocator",it.message.toString())
                locationListener.onGeoLocationFail(it.message?:"")
            }.addOnCanceledListener {
                Log.e("WeatherForecastGeoLocator", "Cancelled")
                locationListener.onGeoLocationFail(context.getString(R.string.city_locating_cancelled))
            }
        } catch (sec: SecurityException) {
            locationListener.onNoLocationPermission()
        }
    }
}