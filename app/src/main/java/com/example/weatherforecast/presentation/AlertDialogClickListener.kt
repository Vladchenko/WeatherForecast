package com.example.weatherforecast.presentation

import android.location.Location

/**
 * Alert dialog buttons click listener
 */
interface AlertDialogClickListener {
    /**
     * Positive button click, having a [city] and its lat long coordinate as [location] passed
     */
    fun onPositiveClick(city:String, location: Location?)

    /**
     * Negative button click
     */
    fun onNegativeClick()
}