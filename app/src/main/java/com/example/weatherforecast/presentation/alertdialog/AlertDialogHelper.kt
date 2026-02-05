package com.example.weatherforecast.presentation.alertdialog

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.presentation.alertdialog.delegates.CitySelectionAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.GeoLocationAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.LocationPermissionAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.NoLocationPermissionPermanentlyAlertDialogDelegate

/**
 * Permission to define location alert dialog.
 *
 * @property context android.context.Context
 */
class AlertDialogHelper(private val context: Context) {

    /**
     * Get alert dialog builder, passing [onPositiveClick], [onNegativeClick] callbacks.
     */
    fun getLocationPermissionAlertDialogBuilder(
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ): AlertDialog.Builder {
        Log.d(TAG, "No location permission, respective alertDialog shown")
        val locationPermissionAlertDialogDelegate = LocationPermissionAlertDialogDelegate(
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        )
        return locationPermissionAlertDialogDelegate.getAlertDialogBuilder(context)
    }

    /**
     * Get alert dialog builder, passing [message], [onPositiveClick], [onNegativeClick] callbacks.
     */
    fun getNoLocationPermissionPermanentlyAlertDialogBuilder(
        message: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ): AlertDialog.Builder {
        Log.d(TAG, "No location permission permanently")
        val alertDialogDelegate =
            NoLocationPermissionPermanentlyAlertDialogDelegate(
                message,
                onPositiveClick = onPositiveClick,
                onNegativeClick = onNegativeClick
            )
        return alertDialogDelegate.getAlertDialogBuilder(context)
    }

    /**
     * Get alert dialog builder, passing [city] and [onPositiveClick], [onNegativeClick] callbacks.
     */
    fun getGeoLocationAlertDialogBuilder(
        city: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ): AlertDialog.Builder {
        Log.d(TAG, "Current geo location alertDialog shown")
        val geoLocationAlertDialogDelegate = GeoLocationAlertDialogDelegate(
            city,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        )
        return geoLocationAlertDialogDelegate.getAlertDialogBuilder(context)
    }

    /**
     * Get alert dialog builder, passing [city] and [onPositiveClick], [onNegativeClick] callbacks.
     */
    fun getAlertDialogBuilderToChooseAnotherCity(
        city: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ): AlertDialog.Builder {
        return CitySelectionAlertDialogDelegate(
            city,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        ).getAlertDialogBuilder(context)
    }

    companion object {
        private const val TAG = "AlertDialogHelper"
    }
}