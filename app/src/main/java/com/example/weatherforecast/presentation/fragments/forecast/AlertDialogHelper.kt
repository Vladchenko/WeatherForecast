package com.example.weatherforecast.presentation.fragments.forecast

import android.content.Context
import android.util.Log
import com.example.weatherforecast.presentation.alertdialog.CitySelectionAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.GeoLocationAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.LocationPermissionAlertDialogDelegate

/**
 * @author Алексей Кузнецов 12.09.2022.
 */
class AlertDialogHelper(
    private val context: Context
) {
    fun showLocationPermissionAlertDialog(
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ) {
        Log.d("AlertDialogHelper", "No location permission, respective alertDialog shown")
        val locationPermissionAlertDialogDelegate = LocationPermissionAlertDialogDelegate(
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        )
        locationPermissionAlertDialogDelegate.showAlertDialog(context)
    }

    fun showGeoLocationAlertDialog(
        city: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ) {
        Log.d("AlertDialogHelper", "Current geo location alertDialog shown")
        val geoLocationAlertDialogDelegate = GeoLocationAlertDialogDelegate(
            city,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        )
        geoLocationAlertDialogDelegate.showAlertDialog(context)
    }

    fun showAlertDialogToChooseAnotherCity(
        city: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ) {
        CitySelectionAlertDialogDelegate(
            city,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        ).showAlertDialog(context)
    }
}