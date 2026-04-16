package io.github.vladchenko.weatherforecast.core.utils

/**
 * Utils helper methods.
 */
object TemperatureConversionUtils {

    /**
     * Convert [kelvinTemp] to celsius degrees.
     */
    fun convertKelvinToCelsiusDegrees(kelvinTemp: Double) = kelvinTemp - 273.15

    /**
     * Convert [kelvinTemp] to fahrenheit degrees.
     */
    fun convertKelvinToFahrenheitDegrees(kelvinTemp: Double) = 1.8 * (kelvinTemp - 273) + 32.0
}