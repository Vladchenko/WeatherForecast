package com.example.weatherforecast.presentation.view.fragments.forecast

import android.location.Address
import android.location.Location
import android.location.LocationManager

/**
 * Converts this [Address] object into a [Location] using latitude and longitude.
 *
 * The resulting [Location] is tagged with the [LocationManager.NETWORK_PROVIDER]
 * as its provider, regardless of the original source of the address.
 *
 * @return A new [Location] instance with coordinates matching this address
 */
fun Address.toLocation(): Location{
    val location = Location(LocationManager.NETWORK_PROVIDER)
    location.latitude = latitude
    location.longitude = longitude
    return location
}