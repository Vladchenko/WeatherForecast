package io.github.vladchenko.weatherforecast.feature.geolocation.domain

import io.github.vladchenko.weatherforecast.core.domain.model.Coordinate

/**
 * Listener that retrieves a geo location for a device user runs this app on.
 */
interface GeoLocationListener {
    /**
     * Callback for a geo location success. Feeds [coordinate] with a latitude and longitude in it.
     */
    fun onDeviceGeoLocationSuccess(coordinate: Coordinate)

    /**
     * When geo location failed, inform about it with [errorMessage].
     */
    fun onDeviceGeoLocationFail(errorMessage: String)

    /**
     * When no permission for geo location.
     */
    fun onNoGeoLocationPermission()
}