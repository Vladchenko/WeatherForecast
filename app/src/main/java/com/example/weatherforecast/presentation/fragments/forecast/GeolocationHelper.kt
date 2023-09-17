package com.example.weatherforecast.presentation.fragments.forecast

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.Locale

/**
 * Helper methods for [android.location.Location]
 *
 * @param context android.content.Context
 */
class GeolocationHelper(private val context: Context) {
    /**
     * Get area name (i.e. city) by [location]
     */
    suspend fun loadCityNameByLocation(location: Location): String = with(Dispatchers.IO) {
        val geoCoder = Geocoder(context, Locale.getDefault())
        var locality: String
        while (true) {
            try {
                locality = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                    ?.first()?.locality ?: ""
                break
            } catch (ex: IOException) {
                delay(500)
                Log.e("GeoLocationHelper", ex.toString())
                continue
            }
        }
        return locality
    }

    /**
     * Define android.location.Location for [city]
     */
    suspend fun defineLocationByCity(city: String): Location = with(Dispatchers.IO) {
        val geoCoder = Geocoder(context, Locale.getDefault())
        var location: Location
        while (true) {
            try {
                location =
                    geoCoder.getFromLocationName(city, 1)?.first()?.toLocation() ?: Location("")
                break
            } catch (e: IOException) {
                delay(500)
                continue
            }
        }
        return location
    }
}