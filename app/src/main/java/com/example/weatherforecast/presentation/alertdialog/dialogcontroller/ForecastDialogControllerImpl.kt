package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper
import com.example.weatherforecast.presentation.alertdialog.delegates.GeoLocationAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.LocationPermissionAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.NoLocationPermissionPermanentlyAlertDialogDelegate
import com.example.weatherforecast.presentation.alertdialog.delegates.SelectedCityNotFoundAlertDialogDelegate
import com.example.weatherforecast.utils.ResourceManager

/**
 * Implementation of [ForecastDialogController]
 *
 * @constructor
 * @property resourceManager to provide localized strings for dialogs.
 * @property dialogHelper to build dialogs.
 */
class ForecastDialogControllerImpl(
    private val resourceManager: ResourceManager,
    private val dialogHelper: AlertDialogHelper,
) : ForecastDialogController {

    override fun showChosenCityNotFound(city: String, onPositive: () -> Unit) {
        dialogHelper.getAlertDialogBuilder(
            SelectedCityNotFoundAlertDialogDelegate(
                city,
                resourceManager,
                onPositiveClick = onPositive,
                onNegativeClick = {}
            )
        ).show()
    }

    override fun showLocationDefined(
        message: String,
        onPositive: (String) -> Unit,
        onNegative: () -> Unit
    ) {
        dialogHelper.getAlertDialogBuilder(
            GeoLocationAlertDialogDelegate(
                message,
                resourceManager,
                onPositiveClick = onPositive,
                onNegativeClick = onNegative
            )
        ).show()
    }

    override fun showNoPermission(onPositive: () -> Unit, onNegative: () -> Unit) {
        dialogHelper.getAlertDialogBuilder(
            LocationPermissionAlertDialogDelegate(
                resourceManager,
                onPositiveClick = onPositive,
                onNegativeClick = onNegative
            )
        ).show()
    }

    override fun showPermissionPermanentlyDenied(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ) {
        dialogHelper.getAlertDialogBuilder(
            NoLocationPermissionPermanentlyAlertDialogDelegate(
                resourceManager,
                onPositiveClick = { onPositive() },
                onNegativeClick = { onNegative() }
            )
        ).show()
    }
}
