package com.example.weatherforecast.presentation

/**
 * Alert dialog buttons click listener
 */
interface AlertDialogClickListener {
    /**
     * Positive button click, having a [city] passed
     */
    fun onPositiveClick(city:String)

    /**
     * Negative button click
     */
    fun onNegativeClick()
}