package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

import com.example.weatherforecast.presentation.alertdialog.AlertDialogFactory
import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper

/**
 * Creates [ForecastDialogController].
 *
 * @property alertDialogFactory to provide alert dialogs
 * @property dialogHelper to provide alert dialog builders
 */
class ForecastDialogControllerFactory(
    private val alertDialogFactory: AlertDialogFactory,
    private val dialogHelper: AlertDialogHelper
) {
    /**
     * Creates [ForecastDialogController]
     */
    fun create(): ForecastDialogController =
        ForecastDialogControllerImpl(
            alertDialogFactory = alertDialogFactory,
            dialogHelper = dialogHelper)
}