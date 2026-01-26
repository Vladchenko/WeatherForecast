package com.example.weatherforecast.presentation.view.fragments.forecast

import android.location.Address
import android.location.Location
import android.location.LocationManager

/**
 * Helper methods for [android.location.Address]
 */
fun Address.toLocation(): Location{
    val location = Location(LocationManager.NETWORK_PROVIDER)
    location.latitude = latitude
    location.longitude = longitude
    return location
}