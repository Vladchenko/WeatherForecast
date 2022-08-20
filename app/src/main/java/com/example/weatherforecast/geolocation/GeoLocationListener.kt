package com.example.weatherforecast.geolocation

import android.location.Location

/**
 * Listener that retrieves a current geo location.
 */
interface GeoLocationListener {
    /**
     * Retrieve a geo location with a lat,lon [location] provided.
     */
    fun onCurrentGeoLocationSuccess(location: Location)

    /**
     * When geo location failed.
     */
    fun onCurrentGeoLocationFail(errorMessage: String)

    /**
     * When no permission for geo location.
     */
    fun onNoGeoLocationPermission()
}