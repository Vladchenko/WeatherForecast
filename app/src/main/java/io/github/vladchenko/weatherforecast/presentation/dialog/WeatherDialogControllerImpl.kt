package io.github.vladchenko.weatherforecast.presentation.dialog

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogHelper
import javax.inject.Inject

/**
 * Implementation of [WeatherDialogController] responsible for displaying weather-related dialogs.
 *
 * This class acts as a mediator between the UI layer and the dialog factory/helper components.
 * It uses [WeatherDialogFactory] to create specific dialog types and [AlertDialogHelper] to show them,
 * ensuring consistent presentation and behavior across the application.
 *
 * Each method corresponds to a specific user scenario (e.g., city not found, permission denied)
 * and abstracts away the complexity of dialog creation and configuration.
 *
 * Dialogs are always shown on the main thread to prevent threading violations.
 *
 * @property dialogFactory Factory for creating pre-configured weather-specific dialogs
 * @property dialogHelper Helper for building and displaying standard alert dialogs
 */
class WeatherDialogControllerImpl @Inject constructor(
    private val dialogFactory: WeatherDialogFactory,
    private val dialogHelper: AlertDialogHelper
) : WeatherDialogController {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var activityContext: Context? = null

    /**
     * Sets the Activity context for dialog creation.
     *
     * Must be called before showing any dialogs to ensure they use the AppCompat theme.
     *
     * @param context The Activity context with AppCompat theme
     */
    fun setActivityContext(context: Context) {
        activityContext = context
    }

    override fun showChosenCityNotFound(
        city: String,
        onPositiveClick: () -> Unit
    ) {
        showDialogOnMainThread {
            dialogHelper.showDialog(
                dialogFactory.createCityNotFoundDialog(city, onPositiveClick),
                activityContext
            )
        }
    }

    override fun showLocationDefined(
        city: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ) {
        showDialogOnMainThread {
            dialogHelper.showDialog(
                dialogFactory.createGeoLocationConfirmationDialog(city, onPositiveClick, onNegativeClick),
                activityContext
            )
        }
    }

    override fun showNoPermission(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        showDialogOnMainThread {
            dialogHelper.showDialog(
                dialogFactory.createPermissionDialog(onPositiveClick, onNegativeClick),
                activityContext
            )
        }
    }

    override fun showPermissionPermanentlyDenied(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        showDialogOnMainThread {
            dialogHelper.showDialog(
                dialogFactory.createPermissionPermanentlyDeniedDialog(onPositiveClick, onNegativeClick),
                activityContext
            )
        }
    }

    override fun showGeoLocationError(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        showDialogOnMainThread {
            dialogHelper.showDialog(
                dialogFactory.createGeoLocationErrorDialog(onPositiveClick, onNegativeClick),
                activityContext
            )
        }
    }

    private fun showDialogOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post { action() }
        }
    }
}