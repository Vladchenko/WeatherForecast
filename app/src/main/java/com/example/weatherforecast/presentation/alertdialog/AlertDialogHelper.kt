package com.example.weatherforecast.presentation.alertdialog

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.presentation.alertdialog.delegates.GeoLocationAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.LocationPermissionAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.NoLocationPermissionPermanentlyAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.SelectedCityNotFoundAlertDialogDelegate
import com.example.weatherforecast.utils.ResourceManager

/**
 * Alert dialog builders helper.
 *
 * @constructor
 * @property context android.context.Context
 */
class AlertDialogHelper(private val context: Context) {

    /**
     * Get alert dialog builder, passing [resourceManager] to get string resources
     * and [onPositiveClick], [onNegativeClick] callbacks for ok and cancel buttons.
     */
    fun getLocationPermissionAlertDialogBuilder(
        resourceManager:ResourceManager,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ): AlertDialog.Builder {
        Log.d(TAG, "No location permission, respective alertDialog shown")
        val locationPermissionAlertDialogDelegate = LocationPermissionAlertDialogDelegate(
            resourceManager = resourceManager,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        )
        return locationPermissionAlertDialogDelegate.getAlertDialogBuilder(context)
    }

    /**
     * Get alert dialog builder, passing [resourceManager] to get string resources
     * and [onPositiveClick], [onNegativeClick] callbacks for ok and cancel buttons.
     */
    fun getNoLocationPermissionPermanentlyAlertDialogBuilder(
        resourceManager:ResourceManager,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ): AlertDialog.Builder {
        Log.d(TAG, "No location permission permanently")
        val alertDialogDelegate =
            NoLocationPermissionPermanentlyAlertDialogDelegate(
                resourceManager = resourceManager,
                onPositiveClick = onPositiveClick,
                onNegativeClick = onNegativeClick
            )
        return alertDialogDelegate.getAlertDialogBuilder(context)
    }

    /**
     * Get alert dialog builder, passing [city], [resourceManager] to get string resources
     * and [onPositiveClick], [onNegativeClick] callbacks for ok and cancel buttons.
     */
    fun getGeoLocationAlertDialogBuilder(
        city: String,
        resourceManager:ResourceManager,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ): AlertDialog.Builder {
        Log.d(TAG, "Current geo location alertDialog shown")
        val geoLocationAlertDialogDelegate = GeoLocationAlertDialogDelegate(
            city,
            resourceManager = resourceManager,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        )
        return geoLocationAlertDialogDelegate.getAlertDialogBuilder(context)
    }

    /**
     * Get alert dialog builder, passing [city], [resourceManager] to get string resources
     * and [onPositiveClick], [onNegativeClick] callbacks for ok and cancel buttons.
     */
    fun getAlertDialogBuilderToChooseAnotherCity(
        city: String,
        resourceManager:ResourceManager,
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ): AlertDialog.Builder {
        return SelectedCityNotFoundAlertDialogDelegate(
            city,
            resourceManager = resourceManager,
            onPositiveClick = onPositiveClick,
            onNegativeClick = onNegativeClick
        ).getAlertDialogBuilder(context)
    }

    companion object {
        private const val TAG = "AlertDialogHelper"
    }
}