package com.example.weatherforecast.geolocation

import android.app.Activity
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
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
class WeatherForecastGeoLocator(private val permissionDelegate: GeoLocationPermissionDelegate) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Define device geo location, using [activity] and deliver callback through [locationListener].
     */
    fun getCityByLocation(activity: Activity, locationListener: GeoLocationListener) {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
                Log.d("WeatherForecastGeoLocator", location.toString())
                if (location == null) {
                    locationListener.onGeoLocationFail()
                } else {
                    Log.d("WeatherForecastGeoLocator", location.toString())
                        val geoCoder = Geocoder(activity as Context, Locale.getDefault())
                        locationListener.onGeoLocationSuccess(
                            location,
                            geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
                        )
                }
            }.addOnFailureListener {
                // TODO Throw exception and process it on upper level
                Log.e("WeatherForecastGeoLocator",it.message.toString())
            }.addOnCanceledListener {
                // TODO Throw exception and process it on upper level
                Log.e("WeatherForecastGeoLocator", "Cancelled")
            }
        } catch (sec: SecurityException) {
            permissionDelegate.getPermissionForGeoLocation(activity)
            getCityByLocation(activity, locationListener)
        }
    }
}