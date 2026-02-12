package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

import com.example.weatherforecast.presentation.alertdialog.AlertDialogFactory
import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper

/**
 * Implementation of [ForecastDialogController]
 *
 * @constructor
 * @property alertDialogFactory to provide alert dialogs.
 * @property dialogHelper to build dialogs.
 */
class ForecastDialogControllerImpl(
    private val alertDialogFactory: AlertDialogFactory,
    private val dialogHelper: AlertDialogHelper,
) : ForecastDialogController {

    override fun showChosenCityNotFound(
        city: String,
        onPositiveClick: () -> Unit
    ) {
        dialogHelper.createDialog(
            alertDialogFactory.createCityNotFoundDelegate(
                city,
                onPositiveClick,
                { }
            )
        ).show()
    }

    override fun showLocationDefined(
        message: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ) {
        dialogHelper.createDialog(
            alertDialogFactory.createGeoLocationDelegate(
                message,
                onPositive = onPositiveClick,
                onNegative = onNegativeClick
            )
        ).show()
    }

    override fun showNoPermission(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        dialogHelper.createDialog(
            alertDialogFactory.createLocationPermissionDelegate(
                onPositive = onPositiveClick,
                onNegative = onNegativeClick
            )
        ).show()
    }

    override fun showPermissionPermanentlyDenied(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        dialogHelper.createDialog(
            alertDialogFactory.createPermissionPermanentlyDeniedDelegate(
                onPositive = onPositiveClick,
                onNegative = onNegativeClick
            )
        ).show()
    }
}
