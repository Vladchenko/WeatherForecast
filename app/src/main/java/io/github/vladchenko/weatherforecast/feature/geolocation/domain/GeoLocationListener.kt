package io.github.vladchenko.weatherforecast.feature.geolocation.domain

import android.location.Location

/**
 * Listener that retrieves a geo location for a device user runs this app on.
 */
interface GeoLocationListener {
    /**
     * Callback for a geo location success. Feeds [location] with a latitude and longitude in it.
     */
    fun onDeviceGeoLocationSuccess(location: Location)

    /**
     * When geo location failed, inform about it with [errorMessage].
     */
    fun onDeviceGeoLocationFail(errorMessage: String)

    /**
     * When no permission for geo location.
     */
    fun onNoGeoLocationPermission()
}