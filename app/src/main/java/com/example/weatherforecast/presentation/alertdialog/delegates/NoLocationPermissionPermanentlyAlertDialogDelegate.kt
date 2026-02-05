package com.example.weatherforecast.presentation.alertdialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

/**
 * Shows alert dialog when geo location permission is not granted
 *
 * @param message message to show in a dialog
 * @property onPositiveClick positive click callback
 * @property onNegativeClick negative click callback
 */
class NoLocationPermissionPermanentlyAlertDialogDelegate(
    private val message: String,
    private val onPositiveClick: (String) -> Unit,
    private val onNegativeClick: () -> Unit
) {

    /**
     * Get [AlertDialog.Builder] using [Context]
     */
    fun getAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Geo location permission not granted")
        builder.setMessage(message)
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