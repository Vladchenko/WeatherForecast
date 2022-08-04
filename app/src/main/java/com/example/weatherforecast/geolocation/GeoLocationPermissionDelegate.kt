package com.example.weatherforecast.geolocation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * Grants permission to further get a geo location of a device.
 */
class GeoLocationPermissionDelegate {

    /**
     * Grant permission to get geo location of a device, using [activity].
     */
    fun getPermissionForGeoLocation(activity: Activity):LocationPermission {
        return if (ActivityCompat.checkSelfPermission(activity as Context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(activity as Context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_ASK_PERMISSIONS
            )
            LocationPermission.GRANTED
        } else {
            LocationPermission.ALREADY_PRESENT
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