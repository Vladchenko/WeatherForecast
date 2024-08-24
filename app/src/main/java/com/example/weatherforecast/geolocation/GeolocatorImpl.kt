package com.example.weatherforecast.geolocation

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.presentation.fragments.forecast.toLocation
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.Locale

/**
 * [Geolocator] implementation
 *
 * @param context android.content.Context
 * @param coroutineDispatchers dispatchers for coroutines
 */
class GeolocatorImpl(
    private val context: Context,
    private val coroutineDispatchers: CoroutineDispatchers,
) : Geolocator {

    /**
     * Get city(area) name by [location]
     */
    override suspend fun defineCityNameByLocation(location: Location): String =
        with(coroutineDispatchers.io) {
            val geoCoder = Geocoder(context, Locale.getDefault())
            var locality: String
            while (true) {
                try {
                    locality = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                        ?.first()?.locality.orEmpty()
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
     * Define [android.location.Location] for [city]
     */
    override suspend fun defineLocationByCity(city: String): Location =
        with(coroutineDispatchers.io) {
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