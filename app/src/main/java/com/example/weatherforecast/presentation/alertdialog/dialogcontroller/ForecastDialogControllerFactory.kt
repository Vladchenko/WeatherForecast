package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

import androidx.appcompat.app.AppCompatActivity
import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper
import com.example.weatherforecast.utils.ResourceManager

/**
 * Creates [ForecastDialogController].
 *
 * @property resourceManager to provide needed resources
 * @property dialogHelper to provide alert dialog builders
 */
class ForecastDialogControllerFactory(
    private val resourceManager: ResourceManager,
    private val dialogHelper: AlertDialogHelper
) {
    /**
     * Creates [ForecastDialogController] using [activity].
     */
    fun create(activity: AppCompatActivity): ForecastDialogController =
        ForecastDialogControllerImpl(
            resourceManager = resourceManager,
            dialogHelper = dialogHelper
        ) { activity.window.decorView.rootView }
}