package com.example.weatherforecast.data.api.customexceptions

/**
 * Custom exception class for weather forecast related errors.
 * This exception is thrown when there are issues with weather data retrieval or processing.
 *
 * @property message The error message describing what went wrong
 */
class WeatherException(message: String) : Exception(message)