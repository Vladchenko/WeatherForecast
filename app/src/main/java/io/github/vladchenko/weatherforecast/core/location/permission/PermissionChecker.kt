package io.github.vladchenko.weatherforecast.core.location.permission

/**
 * Interface for checking geo location permissions.
 */
interface PermissionChecker {

    /**
     * Checks if the app has geo location permission.
     */
    fun hasLocationPermission(): Boolean
}