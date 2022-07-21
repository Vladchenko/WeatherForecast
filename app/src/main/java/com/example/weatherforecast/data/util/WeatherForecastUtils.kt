package com.example.weatherforecast.data.util

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
}