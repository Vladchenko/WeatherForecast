package com.example.weatherforecast.presentation.viewmodel.geolocation

import android.location.Location
import android.location.LocationManager

/**
 * Detecting [Location] by [latitude] and [longitude]
 */
fun getLocationByLatLon(latitude: Double, longitude: Double): Location {
    val location = Location(LocationManager.NETWORK_PROVIDER)
    location.latitude = latitude
    location.longitude = longitude
    return location
}