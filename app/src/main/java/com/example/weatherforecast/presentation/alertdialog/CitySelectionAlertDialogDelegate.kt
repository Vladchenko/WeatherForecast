package com.example.weatherforecast.presentation.alertdialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

/**
 * Shows alert dialog and processes its buttons clicks
 *
 * @param city to have a weather forecast for
 * @param onPositiveClick ok button click callback
 * @param onNegativeClick cancel button click callback
 */
class CitySelectionAlertDialogDelegate(private val city: String,
                                       private val onPositiveClick: (String) -> Unit,
                                       private val onNegativeClick: () -> Unit) {

    private lateinit var alertDialog: AlertDialog

    /**
     * Show alert dialog using [android.content.Context]
     */
    fun showAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("City forecast failed")
        builder.setMessage("Forecast for city $city is not available, please choose another city")
        builder.setPositiveButton(android.R.string.ok) { _dialogInterface, _ ->
            positiveButtonClick(
                _dialogInterface
            )
        }
        alertDialog = builder.show()
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