package com.example.weatherforecast.data.api.customexceptions

/**
 * Exception to mark the state when specific city was not found on server.
 *
 * @property city city name
 * @property message custom message
 */
data class CityNotFoundException(val city: String, override val message:String): Exception(message)