package io.github.vladchenko.weatherforecast.presentation.alertdialog.dialogcontroller

import io.github.vladchenko.weatherforecast.presentation.alertdialog.AlertDialogFactory
import io.github.vladchenko.weatherforecast.presentation.alertdialog.AlertDialogHelper

/**
 * Creates [WeatherDialogController].
 *
 * @property alertDialogFactory to provide alert dialogs
 * @property dialogHelper to provide alert dialog builders
 */
class WeatherDialogControllerFactory(
    private val alertDialogFactory: AlertDialogFactory,
    private val dialogHelper: AlertDialogHelper
) {
    /**
     * Creates [WeatherDialogController]
     */
    fun create(): WeatherDialogController =
        WeatherDialogControllerImpl(
            alertDialogFactory = alertDialogFactory,
            dialogHelper = dialogHelper)
}