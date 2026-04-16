package io.github.vladchenko.weatherforecast.feature.geolocation.data

import android.location.Location
import io.github.vladchenko.weatherforecast.core.utils.dispatchers.CoroutineDispatchers
import io.github.vladchenko.weatherforecast.feature.geolocation.data.api.NominatimApi
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationException
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.Geolocator
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