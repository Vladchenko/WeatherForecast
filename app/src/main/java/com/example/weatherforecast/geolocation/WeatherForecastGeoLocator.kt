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
import java.lang.ref.WeakReference
import java.util.Locale

/**
 * Defines location of a device.
 */
class WeatherForecastGeoLocator(private val permissionDelegate: GeoLocationPermissionDelegate) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Define device geo location, using [weakReference] and deliver callback through [locationListener].
     */
    fun getCityByLocation(weakReference: WeakReference<Activity>, locationListener: GeoLocationListener) {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(weakReference.get() as Context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
                Log.d("WeatherForecastGeoLocator", location.toString())
                if (location == null) {
                    locationListener.onGeoLocationFail()
                } else {
                    Log.d("WeatherForecastGeoLocator", location.toString())
                        val geoCoder = Geocoder(weakReference.get() as Context, Locale.getDefault())
                        locationListener.onGeoLocationSuccess(
                            location,
                            geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
                        )
                }
            }
        } catch (sec: SecurityException) {
            permissionDelegate.getPermissionForGeoLocation(weakReference.get() as Activity)
            getCityByLocation(weakReference, locationListener)
        }
    }
}