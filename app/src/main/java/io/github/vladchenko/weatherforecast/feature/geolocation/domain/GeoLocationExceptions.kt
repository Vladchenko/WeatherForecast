package io.github.vladchenko.weatherforecast.feature.geolocation.domain

/**
 * Exception indicating geo location is not available.
 *
 * @constructor to provide dependencies
 *
 * @param cause of the exception.
 */
class GeoLocationException(cause: Exception) : Exception(cause)