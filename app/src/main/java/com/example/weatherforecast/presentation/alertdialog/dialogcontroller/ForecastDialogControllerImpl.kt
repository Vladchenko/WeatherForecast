package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

import android.view.View
import com.example.weatherforecast.presentation.PresentationUtils.closeWith
import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper

/**
 * Implementation using [com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper] and optional [View] for lifecycle binding.
 */
class ForecastDialogControllerImpl(
    private val dialogHelper: AlertDialogHelper,
    private val viewProvider: () -> View?
) : ForecastDialogController {

    override fun showChosenCityNotFound(city: String, onPositive: (String) -> Unit) {
        viewProvider()?.let { view ->
            val alertDialog = dialogHelper.getAlertDialogBuilderToChooseAnotherCity(
                city,
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
                onPositiveClick = { onPositive() },
                onNegativeClick = onNegative
            ).show()
            alertDialog?.setCancelable(false)
            alertDialog?.setCanceledOnTouchOutside(false)
            alertDialog?.closeWith(view)
        }
    }

    override fun showPermissionPermanentlyDenied(message: String,
                                                 onPositive: () -> Unit,
                                                 onNegative: () -> Unit) {
        viewProvider()?.let { view ->
            val alertDialog = dialogHelper.getNoLocationPermissionPermanentlyAlertDialogBuilder(
                message = message,
                onPositiveClick = { onPositive() },
                onNegativeClick = { onNegative() }
            ).show()
            alertDialog?.setCancelable(false)
            alertDialog?.setCanceledOnTouchOutside(false)
            alertDialog?.closeWith(view)
        }
    }
}
