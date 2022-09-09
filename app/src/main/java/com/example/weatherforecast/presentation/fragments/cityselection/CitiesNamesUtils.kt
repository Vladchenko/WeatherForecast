package com.example.weatherforecast.presentation.fragments.cityselection

/**
 * Helper methods for cities names screen.
 */
object CitiesNamesUtils {

    /**
     * Check city name validity
     */
    fun isCityNameValid(city: String) = city.isNotBlank() && !city.contains(',')
}