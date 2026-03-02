package com.example.weatherforecast.geolocation

import android.location.Location
import com.example.weatherforecast.data.api.NominatimApi
import com.example.weatherforecast.data.api.customexceptions.GeoLocationException
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import kotlinx.coroutines.withContext

/**
 * [Geolocator] implementation
 *
 * @param nominatimApi API service interface for communicating with the Nominatim OpenStreetMap service.
 *                      Used to perform:
 *                      - Forward geocoding (city name → coordinates) via [NominatimApi.search]
 *                      - Reverse geocoding (coordinates → city name) via [NominatimApi.reverse]
 *                      Must be provided via dependency injection (e.g., Hilt).
 * @param coroutineDispatchers dispatchers for coroutines
 */
class GeolocatorImpl(
    private val nominatimApi: NominatimApi,
    private val coroutineDispatchers: CoroutineDispatchers
) : Geolocator {

    override suspend fun defineCityNameByLocation(location: Location): String =
        withContext(coroutineDispatchers.io) {
            try {
                val response = nominatimApi.reverse(
                    lat = location.latitude,
                    lon = location.longitude
                )
                response.address.getCityOrLocality()
            } catch (e: Exception) {
                throw GeoLocationException(e)
            }
        }

    override suspend fun defineLocationByCity(city: String): Location =
        withContext(coroutineDispatchers.io) {
            try {
                val results = nominatimApi.search(query = city)
                if (results.isNotEmpty()) {
                    results[0].toAndroidLocation()
                } else {
                    throw GeoLocationException(RuntimeException("City not found: $city"))
                }
            } catch (e: Exception) {
                throw GeoLocationException(e)
            }
        }
}