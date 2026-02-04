package com.example.weatherforecast.presentation.alertdialog

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherforecast.presentation.PresentationUtils.closeWith

/**
 * Encapsulates forecast dialogs
 */
interface ForecastDialogController {

    fun showChosenCityNotFound(city: String, onPositive: (String) -> Unit)

    fun showLocationDefined(
        message: String,
        onPositive: (String) -> Unit,
        onNegative: () -> Unit
    )

    fun showNoPermission(onPositive: () -> Unit, onNegative: () -> Unit)
}

/**
 * Implementation using [AlertDialogHelper] and optional [View] for lifecycle binding.
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
}

/**
 * Factory provided via [com.example.weatherforecast.di.ForecastPresentationModule];
 * call [create] with view provider from Fragment.
 */
class ForecastDialogControllerFactory(
    private val dialogHelper: AlertDialogHelper
) {
    fun create(activity: AppCompatActivity): ForecastDialogController =
        ForecastDialogControllerImpl(dialogHelper)  { activity.window.decorView.rootView }
}
