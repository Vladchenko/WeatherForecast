package com.example.weatherforecast.data.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utils helper methods.
 */
object WeatherForecastUtils {

    /**
     * Convert [kelvinTemp] to celsius degrees.
     */
    fun convertKelvinToCelsiusDegrees(kelvinTemp: Double) = kelvinTemp - 273.15

    /**
     * Convert [kelvinTemp] to fahrenheit degrees.
     */
    fun convertKelvinToFahrenheitDegrees(kelvinTemp: Double) = 1.8 * (kelvinTemp - 273) + 32.0

    /**
     * Format [time] (value in Long) to DATE_FORMAT presentation. Provide an [error] when formatting
     * is not possible.
     */
    fun getCurrentDate(time: String, error: String): String {
        return try {
            SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date(time.toLong() * 1000))
        } catch (ex: Exception) {
            error
        }
    }

    private const val DATE_FORMAT = "dd/MM/yyyy HH:mm:ss"
}