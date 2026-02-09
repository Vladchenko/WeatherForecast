package com.example.weatherforecast.presentation.viewmodel.geolocation

/**
 * Sealed class representing the possible states of location permission in the app.
 *
 * Used to manage and observe the user's location permission status
 * throughout the application lifecycle.
 */
sealed class GeoLocationPermission {

    /**
     * Location permission has been granted by the user.
     *
     * The app can now access the device's location.
     */
    object Granted : GeoLocationPermission()

    /**
     * Location permission has been denied by the user.
     *
     * The app cannot access the device's location.
     * You may request permission again.
     */
    object Denied : GeoLocationPermission()

    /**
     * Location permission has been permanently denied (e.g., via system settings).
     *
     * The user must manually enable the permission in the device's OS settings.
     */
    object PermanentlyDenied : GeoLocationPermission()

    /**
     * Location permission is currently being requested.
     *
     * The system dialog is likely visible, and the user has not yet made a decision.
     */
    object Requested : GeoLocationPermission()
}