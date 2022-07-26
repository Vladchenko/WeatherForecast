package com.example.weatherforecast.geolocation

import android.app.Activity
import android.location.Location

/**
 * Listener that retrieves a geo location.
 */
interface GeoLocationListener {
    /**
     * Retrieve a geo location with a lat,lon [location] and a city [locationName] provided.
     */
    fun onGeoLocationSuccess(activity: Activity, location: Location, locationName: String)
}