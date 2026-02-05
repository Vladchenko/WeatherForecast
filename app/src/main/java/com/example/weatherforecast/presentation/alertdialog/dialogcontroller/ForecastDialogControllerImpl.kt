package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

import android.view.View
import com.example.weatherforecast.presentation.PresentationUtils.closeWith
import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper
import com.example.weatherforecast.utils.ResourceManager

/**
 * Implementation of [ForecastDialogController]
 *
 * @constructor
 * @property resourceManager to provide localized strings for dialogs.
 * @property dialogHelper to build dialogs.
 * @property viewProvider to get the view to close the dialog with.
 */
class ForecastDialogControllerImpl(
    private val resourceManager: ResourceManager,
    private val dialogHelper: AlertDialogHelper,
    private val viewProvider: () -> View?
) : ForecastDialogController {

    override fun showChosenCityNotFound(city: String, onPositive: () -> Unit) {
        viewProvider()?.let { view ->
            val alertDialog = dialogHelper.getAlertDialogBuilderToChooseAnotherCity(
                city,
                resourceManager,
                onPositiveClick = onPositive,
                onNegativeClick = {}
            ).show()
            alertDialog?.setCancelable(false)
            alertDialog?.setCanceledOnTouchOutside(false)
            alertDialog?.closeWith(view)
        }
    }

    override fun showLocationDefined(
        message: String,
        onPositive: (String) -> Unit,
        onNegative: () -> Unit
    ) {
        viewProvider()?.let { view ->
            val alertDialog = dialogHelper.getGeoLocationAlertDialogBuilder(
                message,
                resourceManager,
                onPositiveClick = onPositive,
                onNegativeClick = onNegative
            ).show()
            alertDialog?.setCancelable(false)
            alertDialog?.setCanceledOnTouchOutside(false)
            alertDialog?.closeWith(view)
        }
    }

    override fun showNoPermission(onPositive: () -> Unit, onNegative: () -> Unit) {
        viewProvider()?.let { view ->
            val alertDialog = dialogHelper.getLocationPermissionAlertDialogBuilder(
                resourceManager,
                onPositiveClick = onPositive,
                onNegativeClick = onNegative
            ).show()
            alertDialog?.setCancelable(false)
            alertDialog?.setCanceledOnTouchOutside(false)
            alertDialog?.closeWith(view)
        }
    }

    override fun showPermissionPermanentlyDenied(onPositive: () -> Unit,
                                                 onNegative: () -> Unit) {
        viewProvider()?.let { view ->
            val alertDialog = dialogHelper.getNoLocationPermissionPermanentlyAlertDialogBuilder(
                resourceManager,
                onPositiveClick = { onPositive() },
                onNegativeClick = { onNegative() }
            ).show()
            alertDialog?.setCancelable(false)
            alertDialog?.setCanceledOnTouchOutside(false)
            alertDialog?.closeWith(view)
        }
    }
}
