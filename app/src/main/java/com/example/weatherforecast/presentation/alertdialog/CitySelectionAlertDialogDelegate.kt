package com.example.weatherforecast.presentation.alertdialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

/**
 * Shows alert dialog and processes its buttons clicks
 *
 * @property city to have a weather forecast for
 * @property onPositiveClick ok button click callback
 * @property onNegativeClick cancel button click callback
 */
class CitySelectionAlertDialogDelegate(private val city: String,
                                       private val onPositiveClick: (String) -> Unit,
                                       private val onNegativeClick: () -> Unit) {

    /**
     * Get [AlertDialog.Builder] using [android.content.Context]
     */
    fun getAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("City forecast failed")
        builder.setMessage("Forecast for city $city is not available, please choose another city")
        builder.setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
            positiveButtonClick(
                dialogInterface
            )
        }
        return builder
    }

    private fun positiveButtonClick(dialogInterface: DialogInterface) {
        onPositiveClick(city)
        dialogInterface.dismiss()
    }

    private fun negativeButtonClick(dialogInterface: DialogInterface) {
        onNegativeClick()
        dialogInterface.dismiss()
    }
}