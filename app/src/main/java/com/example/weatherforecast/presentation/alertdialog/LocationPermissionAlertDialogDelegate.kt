package com.example.weatherforecast.presentation.alertdialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

/**
 * Shows alert dialog when geo location permission is not granted
 *
 * @param city to have a weather forecast for
 * @param clickListener for alert dialog
 */
class LocationPermissionAlertDialogDelegate(
    private val onPositiveClick: (String) -> Unit,
    private val onNegativeClick: () -> Unit
) {

    private lateinit var alertDialog: AlertDialog

    /**
     * Show alert dialog using [android.content.Context]
     */
    fun showAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Geo location permission not granted")
        builder.setMessage(
            "This app requires geo location permission to find out your location. " +
                "Please agree to grant permission or quit."
        )
        builder.setPositiveButton(android.R.string.ok) { _dialogInterface, _ ->
            positiveButtonClick(
                _dialogInterface
            )
        }
        builder.setNegativeButton(android.R.string.cancel) { _dialogInterface, _ ->
            negativeButtonClick(
                _dialogInterface
            )
        }
        alertDialog = builder.show()
    }

    private fun positiveButtonClick(dialogInterface: DialogInterface) {
        onPositiveClick("")
        dialogInterface.dismiss()
    }

    private fun negativeButtonClick(dialogInterface: DialogInterface) {
        onNegativeClick()
        dialogInterface.dismiss()
    }
}