package com.example.weatherforecast.geolocation

import android.app.Activity
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.util.Locale

/**
 * Defines location of a device.
 */
class WeatherForecastGeoLocator {

    private lateinit var locationListener: GeoLocationListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Define device geo location, using [activity].
     */
    fun getCityByLocation(activity: Activity, locationListener: GeoLocationListener) {
        this.locationListener = locationListener
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        }).addOnSuccessListener { location: Location? ->
            if (location == null) {
                Toast.makeText(activity, "Cannot get location", Toast.LENGTH_LONG).show()
            } else {
                Log.i("WeatherForecastGeoLocator", location.toString())
                val geoCoder = Geocoder(activity, Locale.getDefault())
                locationListener.onGeoLocationSuccess(
                    activity,
                    location,
                    geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
                )
            }
        }
    }
}