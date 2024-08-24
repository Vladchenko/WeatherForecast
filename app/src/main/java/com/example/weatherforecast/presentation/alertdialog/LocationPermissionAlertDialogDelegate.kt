package com.example.weatherforecast.presentation.alertdialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

/**
 * Shows alert dialog when geo location permission is not granted
 *
 * @property onPositiveClick positive click callback
 * @property onNegativeClick negative click callback
 */
class LocationPermissionAlertDialogDelegate(
    private val onPositiveClick: (String) -> Unit,
    private val onNegativeClick: () -> Unit
) {

    /**
     * Get [AlertDialog.Builder] using [android.content.Context]
     */
    fun getAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Geo location permission not granted")
        builder.setMessage(
            "This app requires geo location permission to find out your location. " +
                "Please agree to grant permission or quit."
        )
        builder.setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
            positiveButtonClick(
                dialogInterface
            )
        }
        builder.setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
            negativeButtonClick(
                dialogInterface
            )
        }
        return builder
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