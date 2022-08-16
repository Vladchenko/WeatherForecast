package com.example.weatherforecast.presentation

/**
 * Alert dialog buttons click listener
 */
interface AlertDialogClickListener {
    /**
     * Positive button click, having a [locationName] passed
     */
    fun onPositiveClick(locationName:String)

    /**
     * Negative button click
     */
    fun onNegativeClick()
}