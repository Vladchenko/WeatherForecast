package com.example.weatherforecast.presentation.alertdialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

/**
 * Alert dialog that asks user if the geo located city is a proper one to provide a weather forecast on.
 * If a user agrees to the located city, its forecast is downloaded, otherwise, user is suggested to choose another city.
 *
 * @param city to point what city geo location has defined.
 * @param onPositiveClick ok button click callback
 * @param onNegativeClick cancel button click callback
 */
class GeoLocationAlertDialogDelegate(private val city: String,
                                     private val onPositiveClick: (String) -> Unit,
                                     private val onNegativeClick: () -> Unit) {

    /**
     * Get [AlertDialog.Builder] using [Context]
     */
    fun getAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Area geo location")
        builder.setMessage("Google Maps defined your location as $city. Do you agree ?")
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
        onPositiveClick(city)
        dialogInterface.dismiss()
    }

    private fun negativeButtonClick(dialogInterface: DialogInterface) {
        onNegativeClick()
        dialogInterface.dismiss()
    }
}