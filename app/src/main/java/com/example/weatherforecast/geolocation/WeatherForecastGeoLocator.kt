package com.example.weatherforecast.geolocation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    /**
     * Define device geo location, using [activity].
     */
    fun getCityByLocation(activity: Activity, locationListener: GeoLocationListener) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        }).addOnSuccessListener { location: Location? ->
            Log.i("WeatherForecastGeoLocator1", location.toString())
            if (location == null) {
                Log.i("WeatherForecastGeoLocator2", location.toString())
                Toast.makeText(activity, "Cannot get location", Toast.LENGTH_LONG).show()
            } else {
                Log.i("WeatherForecastGeoLocator3", location.toString())
                val geoCoder = Geocoder(activity, Locale.getDefault())
                locationListener.onGeoLocationSuccess(
                    activity,
                    location,
                    geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
                )
            }
        }
    }

    fun getPermissionForGeoLocation(activity: Activity):LocationPermission {
        if (ActivityCompat.checkSelfPermission(activity as Context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(activity as Context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_ASK_PERMISSIONS
            )
            return LocationPermission.GRANTED
        } else {
            return LocationPermission.ALREADY_PRESENT
        }
    }

    enum class LocationPermission {
        GRANTED,
        ALREADY_PRESENT
    }

    companion object {
        const val REQUEST_CODE_ASK_PERMISSIONS = 100
    }
}