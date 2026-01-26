package com.example.weatherforecast.presentation.viewmodel.geolocation

/**
 * Geo location permission states
 */
sealed class GeoLocationPermission {
    /** Permission is granted and thus user can get geo location of used device */
    object Granted : GeoLocationPermission()
    /** Permission is denied and thus user can't get geo location of used device */
    object Denied : GeoLocationPermission()
    /** Permission is denied permanently and thus user has to enable it in android os settings */
    object PermanentlyDenied : GeoLocationPermission()
    /** Permission is requested and then user has to approve or disapprove for android os to grant it */
    object Requested : GeoLocationPermission()
}