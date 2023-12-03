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
 * Defines geo location of a device.
 */
class WeatherForecastGeoLocator {

    /**
     * Define current geo location, sending callbacks through [locationListener].
     */
    fun defineCurrentLocation(appContext: Context, locationListener: GeoLocationListener) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
                Log.d("WeatherForecastGeoLocator", location.toString())
                if (location == null) {
                    locationListener.onCurrentGeoLocationFail(appContext.getString(R.string.geo_location_fail_error_text))
                } else {
                    Log.d("WeatherForecastGeoLocator", location.toString())
                    locationListener.onCurrentGeoLocationSuccess(location)
                }
            }.addOnFailureListener {
                Log.e("WeatherForecastGeoLocator", it.message.toString())
                locationListener.onCurrentGeoLocationFail(it.message.orEmpty())
            }.addOnCanceledListener {
                Log.e("WeatherForecastGeoLocator", "Cancelled")
                locationListener.onCurrentGeoLocationFail(appContext.getString(R.string.city_locating_cancelled))
            }
        } catch (sec: SecurityException) {
            locationListener.onNoGeoLocationPermission()
        }
    }
}