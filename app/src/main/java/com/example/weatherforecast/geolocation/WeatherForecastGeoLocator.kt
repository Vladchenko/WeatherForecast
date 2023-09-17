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
class WeatherForecastGeoLocator {

    private var isReleased = false

    /**
     * Define current geo location, sending callbacks through [locationListener].
     */
    fun defineCurrentLocation(appContext: Context, locationListener: GeoLocationListener) {
        if (isReleased) return
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
                if (isReleased) return@addOnSuccessListener
                Log.d("WeatherForecastGeoLocator", location.toString())
                if (location == null) {
                    locationListener.onCurrentGeoLocationFail(appContext.getString(R.string.geo_location_fail_error_text))
                } else {
                    Log.d("WeatherForecastGeoLocator", location.toString())
                    locationListener.onCurrentGeoLocationSuccess(location)
                }
            }.addOnFailureListener {
                if (isReleased) return@addOnFailureListener
                Log.e("WeatherForecastGeoLocator", it.message.toString())
                locationListener.onCurrentGeoLocationFail(it.message ?: "")
            }.addOnCanceledListener {
                if (isReleased) return@addOnCanceledListener

                Log.e("WeatherForecastGeoLocator", "Cancelled")
                locationListener.onCurrentGeoLocationFail(appContext.getString(R.string.city_locating_cancelled))
            }
        } catch (sec: SecurityException) {
            locationListener.onNoGeoLocationPermission()
        }
    }

    fun release() {
        isReleased = true
    }
}