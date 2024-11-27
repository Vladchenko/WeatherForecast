package com.example.weatherforecast.geolocation

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.example.weatherforecast.data.api.customexceptions.GeoLocationException
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.presentation.fragments.forecast.toLocation
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
            val locality: String
            try {
                locality = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                    ?.first()?.locality.orEmpty()
            } catch (ex: IOException) {
                throw GeoLocationException(ex)
            }
            return locality
        }

    /**
     * Define [android.location.Location] for [city]
     */
    override suspend fun defineLocationByCity(city: String): Location =
        with(coroutineDispatchers.io) {
            val geoCoder = Geocoder(context, Locale.getDefault())
            val location: Location
            try {
                location =
                    geoCoder.getFromLocationName(city, 1)?.first()?.toLocation()
                        ?: Location("")
            } catch (ex: IOException) {
                throw GeoLocationException(ex)
            }
            return location
        }
}