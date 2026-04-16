package io.github.vladchenko.weatherforecast.feature.geolocation.domain

/**
 * Interface for checking geo location permissions.
 */
interface PermissionChecker {

    /**
     * Checks if the app has geo location permission.
     */
    fun hasLocationPermission(): Boolean
}