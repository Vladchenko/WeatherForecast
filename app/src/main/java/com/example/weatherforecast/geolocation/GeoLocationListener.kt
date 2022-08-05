package com.example.weatherforecast.geolocation

import android.content.Context
import android.location.Location

/**
 * Listener that retrieves a geo location.
 */
interface GeoLocationListener {
    /**
     * Retrieve a geo location with a lat,lon [location] and a city [locationName] provided.
     */
    fun onGeoLocationSuccess(context: Context, location: Location, locationName: String)

    /**
     * When geo location failed.
     */
    fun onGeoLocationFail()
}