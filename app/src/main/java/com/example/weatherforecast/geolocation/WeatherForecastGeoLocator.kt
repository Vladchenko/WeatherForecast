package com.example.weatherforecast.geolocation

import android.app.Activity
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

/**
 * Defines location of a device.
 */
class WeatherForecastGeoLocator {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Define device geo location, using [activity].
     */
    fun getLocation(activity: Activity): Location? {
        var currentLocation: Location? = null
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        }).addOnSuccessListener { location: Location? ->
            if (location == null) {
                Toast.makeText(activity, "Cannot get location", Toast.LENGTH_LONG).show()
            } else {
                Log.i("WeatherForecastGeoLocator", location.toString())
                currentLocation = location
            }
        }
        Log.i("WeatherForecastGeoLocator", currentLocation.toString())
        return currentLocation
    }
}