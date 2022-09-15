package com.example.weatherforecast.presentation.viewmodel.forecast

import android.location.Location
import android.location.LocationManager

/**
 * Getting [Location] from [latitude] and [longitude]
 */
fun getLocationByLatLon(latitude: Double, longitude: Double): Location {
    val location = Location(LocationManager.NETWORK_PROVIDER)
    location.latitude = latitude
    location.longitude = longitude
    return location
}