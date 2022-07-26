package com.example.weatherforecast.geolocation

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

/**
 * Shows alert dialog and processes its buttons clicks
 */
class AlertDialogDelegate(private val city: String,
                          private val clickListener: AlertDialogClickListener) {

    /**
     * Show alert dialog using [android.content.Context]
     */
    fun showAlertDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Area geo location")
        builder.setMessage("Google Maps defined your location as $city. Do you agree ?")
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
        builder.show()
    }

    private fun positiveButtonClick(dialogInterface: DialogInterface) {
        clickListener.onPositiveClick(city)
        dialogInterface.dismiss()
    }

    private fun negativeButtonClick(dialogInterface: DialogInterface) {
        clickListener.onNegativeClick()
        dialogInterface.dismiss()
    }
}