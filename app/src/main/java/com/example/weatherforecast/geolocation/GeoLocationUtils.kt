package com.example.weatherforecast.geolocation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * Checks if app has a geo location permission.
 *
 * @param context android.content.Context
 */
fun hasPermissionForGeoLocation(context: Context) =
    ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED