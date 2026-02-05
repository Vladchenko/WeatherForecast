package com.example.weatherforecast.presentation.alertdialog.dialogcontroller

import androidx.appcompat.app.AppCompatActivity
import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper

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