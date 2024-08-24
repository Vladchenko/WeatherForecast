package com.example.weatherforecast.geolocation

import android.location.Location

/**
 * Listener that retrieves a current geo location.
 */
interface GeoLocationListener {
    /**
     * Callback for a geo location success. Feeds [location] with a latitude and longitude in it.
     */
    fun onCurrentGeoLocationSuccess(location: Location)

    /**
     * When geo location failed, inform about it with [errorMessage].
     */
    fun onCurrentGeoLocationFail(errorMessage: String)

    /**
     * When no permission for geo location.
     */
    fun onNoGeoLocationPermission()
}