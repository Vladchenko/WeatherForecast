package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

import com.example.weatherforecast.presentation.alertdialog.AlertDialogFactory
import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper

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