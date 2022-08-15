package com.example.weatherforecast.data.api.customexceptions

/**
 * Exception to mark the state when specific city was not found on server.
 */
class CityNotFoundException(message:String): Exception(message)