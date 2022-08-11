package com.example.weatherforecast.data.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TODO
 */
object WeatherForecastUtils {

    /**
     *
     */
    fun getCelsiusFromKelvinTemperature(kelvinTemp: Double) = kelvinTemp - 273.15

    /**
     *
     */
    fun getFahrenheitFromKelvinTemperature(kelvinTemp: Double) = 1.8 * (kelvinTemp - 273) + 32.0

    /**
     *
     */
    fun getCurrentDate(time: String, error: String): String {
        return try {
            SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date(time.toLong() * 1000))
        } catch (ex: Exception) {
            error
        }
    }

    private const val DATE_FORMAT = "dd/MM/yyyy"
}