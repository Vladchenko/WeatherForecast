package com.example.weatherforecast.presentation.alertdialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.R
import com.example.weatherforecast.utils.ResourceManager

/**
 * Shows alert dialog for a server city lookup failure and processes its buttons clicks
 *
 * @property city to have a weather forecast for
 * @property resourceManager to get strings from resources
 * @property onPositiveClick ok button click callback
 * @property onNegativeClick cancel button click callback
 */
class SelectedCityNotFoundAlertDialogDelegate(private val city: String,
                                              private val resourceManager: ResourceManager,
                                              private val onPositiveClick: () -> Unit,
                                              private val onNegativeClick: () -> Unit) {

    /**
     * Get [AlertDialog.Builder] using [Context]
     */
    fun getAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(resourceManager.getString(R.string.no_selected_city_forecast, city))
        builder.setMessage(resourceManager.getString(R.string.no_selected_city_forecast_message))
        builder.setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
            positiveButtonClick(
                dialogInterface
            )
        }
        return builder
    }

    private fun positiveButtonClick(dialogInterface: DialogInterface) {
        onPositiveClick()
        dialogInterface.dismiss()
    }

    private fun negativeButtonClick(dialogInterface: DialogInterface) {
        onNegativeClick()
        dialogInterface.dismiss()
    }
}