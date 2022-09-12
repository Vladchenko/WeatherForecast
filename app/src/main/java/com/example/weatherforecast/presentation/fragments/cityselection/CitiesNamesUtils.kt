package com.example.weatherforecast.presentation.fragments.cityselection

/**
 * Helper methods for cities names screen.
 */
object CitiesNamesUtils {

    /**
     * Check city name validity
     */
    fun isCityNameValid(city: String):Boolean {
        if ((city.length < 3) && city.contains(',')) {
            return false
        }
        return true
    }
}