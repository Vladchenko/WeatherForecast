package io.github.vladchenko.weatherforecast.core.location.permission

import android.content.Context
import io.github.vladchenko.weatherforecast.core.location.geolocation.hasPermissionForGeoLocation

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