package io.github.vladchenko.weatherforecast.feature.geolocation.data

import io.github.vladchenko.weatherforecast.core.domain.model.Coordinate
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

    override suspend fun defineCityNameByLocation(coordinate: Coordinate): String =
        withContext(coroutineDispatchers.io) {
            try {
                val response = nominatimApi.reverse(
                    lat = coordinate.latitude,
                    lon = coordinate.longitude
                )
                response.address.getCityOrLocality()
            } catch (e: Exception) {
                throw GeoLocationException(e)
            }
        }

    override suspend fun defineLocationByCity(city: String): Coordinate =
        withContext(coroutineDispatchers.io) {
            try {
                val results = nominatimApi.search(query = city)
                if (results.isNotEmpty()) {
                    results[0].toCoordinate()
                } else {
                    throw GeoLocationException(RuntimeException("City not found: $city"))
                }
            } catch (e: Exception) {
                throw GeoLocationException(e)
            }
        }
}