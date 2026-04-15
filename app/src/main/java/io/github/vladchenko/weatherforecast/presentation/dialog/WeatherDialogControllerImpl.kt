package io.github.vladchenko.weatherforecast.presentation.dialog

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
 * @property dialogFactory Factory for creating pre-configured weather-specific dialogs
 * @property dialogHelper Helper for building and displaying standard alert dialogs
 */
class WeatherDialogControllerImpl @Inject constructor(
    private val dialogFactory: WeatherDialogFactory,
    private val dialogHelper: AlertDialogHelper
) : WeatherDialogController {

    override fun showChosenCityNotFound(
        city: String,
        onPositiveClick: () -> Unit
    ) {
        dialogHelper.showDialog(
            dialogFactory.createCityNotFoundDialog(city, onPositiveClick)
        )
    }

    override fun showLocationDefined(
        city: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ) {
        dialogHelper.showDialog(
            dialogFactory.createGeoLocationConfirmationDialog(city, onPositiveClick, onNegativeClick)
        )
    }

    override fun showNoPermission(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        dialogHelper.showDialog(
            dialogFactory.createPermissionDialog(onPositiveClick, onNegativeClick)
        )
    }

    override fun showPermissionPermanentlyDenied(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        dialogHelper.showDialog(
            dialogFactory.createPermissionPermanentlyDeniedDialog(onPositiveClick, onNegativeClick)
        )
    }

    override fun showGeoLocationError(
        onPositiveClick: () -> Unit,
        onNegativeClick: () -> Unit
    ) {
        dialogHelper.showDialog(
            dialogFactory.createGeoLocationErrorDialog(onPositiveClick, onNegativeClick)
        )
    }
}