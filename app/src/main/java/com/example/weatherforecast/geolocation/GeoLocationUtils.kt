package com.example.weatherforecast.geolocation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * Checks if app has a geo location permission.
 */
fun Context.hasPermissionForGeoLocation() =
    ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED