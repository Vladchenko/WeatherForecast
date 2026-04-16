package io.github.vladchenko.weatherforecast.feature.geolocation.util

import android.location.Location
import android.location.LocationManager

/**
 * Creates a new [Location] instance using latitude and longitude.
 *
 * @param latitude Latitude in degrees
 * @param longitude Longitude in degrees
 * @return A [Location] object with the given coordinates, using [LocationManager.NETWORK_PROVIDER] as provider
 */
fun createLocation(latitude: Double, longitude: Double): Location =
    Location(LocationManager.NETWORK_PROVIDER).apply {
        this.latitude = latitude
        this.longitude = longitude
    }