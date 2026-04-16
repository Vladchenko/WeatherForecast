package io.github.vladchenko.weatherforecast.feature.geolocation.data.permission

import android.content.Context
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.PermissionChecker
import io.github.vladchenko.weatherforecast.feature.geolocation.util.hasPermissionForGeoLocation

/**
 * Implementation of [io.github.vladchenko.weatherforecast.feature.geolocation.domain.PermissionChecker]
 *
 * @param context to check permissions
 */
class PermissionCheckerImpl(val context: Context): PermissionChecker {
    override fun hasLocationPermission(): Boolean {
        return context.hasPermissionForGeoLocation()
    }
}