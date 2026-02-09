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
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Defines geo location of a device.
 */
class WeatherForecastGeoLocator(
    @param:ApplicationContext private val context: Context
) {

    /**
     * Define current geo location and send callbacks through [locationListener].
     */
    fun defineCurrentLocation(locationListener: GeoLocationListener) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                }).addOnSuccessListener { location: Location? ->
                onDefineLocationSuccess(location, locationListener, context)
            }.addOnFailureListener { exception ->
                onDefineLocationFailure(exception, locationListener)
            }.addOnCanceledListener {
                Log.e(TAG, "Geo location cancelled")
                locationListener.onCurrentGeoLocationFail(context.getString(R.string.current_location_cancelled))
            }
        } catch (sec: SecurityException) {
            Log.e(TAG, sec.message.toString())
            locationListener.onNoGeoLocationPermission()
        }
    }

    private fun onDefineLocationFailure(
        it: Exception,
        locationListener: GeoLocationListener
    ) {
        val errorMessage = it.message.toString()
        Log.e(TAG, errorMessage)
        if (errorMessage.contains("permission")) {
            locationListener.onNoGeoLocationPermission()
        } else {
            locationListener.onCurrentGeoLocationFail(it.message.orEmpty())
        }
    }

    private fun onDefineLocationSuccess(
        location: Location?,
        locationListener: GeoLocationListener,
        appContext: Context
    ) {
        Log.d(TAG, location.toString())
        if (location == null) {
            locationListener.onCurrentGeoLocationFail(appContext.getString(R.string.geo_location_fail_error_text))
        } else {
            Log.d(TAG, location.toString())
            locationListener.onCurrentGeoLocationSuccess(location)
        }
    }

    companion object {
        const val TAG = "WeatherForecastGeoLocator"
    }
}