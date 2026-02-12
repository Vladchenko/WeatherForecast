package com.example.weatherforecast.presentation.alertdialog

import com.example.weatherforecast.presentation.alertdialog.delegates.AlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.GeoLocationAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.LocationPermissionAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.NoLocationPermissionPermanentlyAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.SelectedCityNotFoundAlertDialogDelegate
import com.example.weatherforecast.utils.ResourceManager

class AlertDialogFactory(
    private val resourceManager: ResourceManager
) {
    fun createLocationPermissionDelegate(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return LocationPermissionAlertDialogDelegate(resourceManager, onPositive, onNegative)
    }

    fun createPermissionPermanentlyDeniedDelegate(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return NoLocationPermissionPermanentlyAlertDialogDelegate(
            resourceManager,
            { onPositive() },
            onNegative
        )
    }

    fun createGeoLocationDelegate(
        city: String,
        onPositive: (String) -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return GeoLocationAlertDialogDelegate(city, resourceManager, onPositive, onNegative)
    }

    fun createCityNotFoundDelegate(
        city: String,
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ): AlertDialogDelegate {
        return SelectedCityNotFoundAlertDialogDelegate(
            city,
            resourceManager,
            onPositive,
            onNegative
        )
    }
}