/**
 * Utility functions for location-related operations.
 */
package io.github.vladchenko.weatherforecast.feature.currentweather.presentation

import android.location.Location
import android.location.LocationManager

/**
 * Creates a synthetic [Location] object with the specified coordinates.
 *
 * The location is initialized with [LocationManager.NETWORK_PROVIDER] by default.
 * This function is typically used to simulate location data when a real location
 * object is required but accessing hardware sensors should be avoided.
 *
 * @param latitude the latitude of the location
 * @param longitude the longitude of the location
 * @return a new [Location] instance populated with the provided coordinates
 */
fun createLocation(latitude: Double, longitude: Double): Location =
    Location(LocationManager.NETWORK_PROVIDER).apply {
        this.latitude = latitude
        this.longitude = longitude
    }