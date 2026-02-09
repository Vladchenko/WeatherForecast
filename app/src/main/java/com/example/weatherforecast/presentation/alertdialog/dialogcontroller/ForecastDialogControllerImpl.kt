package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper
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
        val alertDialog = dialogHelper.getAlertDialogBuilderToChooseAnotherCity(
            city,
            resourceManager,
            onPositiveClick = onPositive,
            onNegativeClick = {}
        ).show()
        alertDialog?.setCancelable(false)
        alertDialog?.setCanceledOnTouchOutside(false)
    }

    override fun showLocationDefined(
        message: String,
        onPositive: (String) -> Unit,
        onNegative: () -> Unit
    ) {
        val alertDialog = dialogHelper.getGeoLocationAlertDialogBuilder(
            message,
            resourceManager,
            onPositiveClick = onPositive,
            onNegativeClick = onNegative
        ).show()
        alertDialog?.setCancelable(false)
        alertDialog?.setCanceledOnTouchOutside(false)
    }

    override fun showNoPermission(onPositive: () -> Unit, onNegative: () -> Unit) {
        val alertDialog = dialogHelper.getLocationPermissionAlertDialogBuilder(
            resourceManager,
            onPositiveClick = onPositive,
            onNegativeClick = onNegative
        ).show()
        alertDialog?.setCancelable(false)
        alertDialog?.setCanceledOnTouchOutside(false)
    }

    override fun showPermissionPermanentlyDenied(
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ) {
        val alertDialog = dialogHelper.getNoLocationPermissionPermanentlyAlertDialogBuilder(
            resourceManager,
            onPositiveClick = { onPositive() },
            onNegativeClick = { onNegative() }
        ).show()
        alertDialog?.setCancelable(false)
        alertDialog?.setCanceledOnTouchOutside(false)
    }
}
