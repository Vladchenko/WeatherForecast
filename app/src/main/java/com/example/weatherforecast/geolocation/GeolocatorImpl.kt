package com.example.weatherforecast.geolocation

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.example.weatherforecast.data.api.customexceptions.GeoLocationException
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.presentation.view.fragments.forecast.toLocation
import kotlinx.coroutines.withContext
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
    @Suppress("DEPRECATION")
    override suspend fun defineCityNameByLocation(location: Location): String =
        withContext(coroutineDispatchers.io) {
            val geoCoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
                addresses?.firstOrNull()?.locality.orEmpty()
            } catch (ex: IOException) {
                throw GeoLocationException(ex)
            }
        }

    /**
     * Define [android.location.Location] for [city]
     */
    @Suppress("DEPRECATION")
    override suspend fun defineLocationByCity(city: String): Location =
        withContext(coroutineDispatchers.io) {
            val geoCoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses = geoCoder.getFromLocationName(city, 1)
                addresses?.firstOrNull()?.toLocation() ?: Location("")
            } catch (ex: IOException) {
                throw GeoLocationException(ex)
            }
        }
}