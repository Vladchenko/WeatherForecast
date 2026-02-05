package com.example.weatherforecast.data.util.permission

import android.content.Context
import com.example.weatherforecast.geolocation.hasPermissionForGeoLocation

/**
 * Implementation of [PermissionChecker]
 *
 * @param context to check permissions
 */
class PermissionCheckerImpl(val context: Context): PermissionChecker {
    override fun hasLocationPermission(): Boolean {
        return context.hasPermissionForGeoLocation()
    }
}