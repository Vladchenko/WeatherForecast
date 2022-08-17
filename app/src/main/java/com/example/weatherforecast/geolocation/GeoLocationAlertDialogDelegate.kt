package com.example.weatherforecast.geolocation

import android.content.Context
import android.content.DialogInterface
import android.location.Location
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.presentation.AlertDialogClickListener

/**
 * Shows alert dialog and processes its buttons clicks
 *
 * @param city to point what city geo location has defined.
 * @param location that geo location has defined
 * @param clickListener for alert dialog buttons
 */
class GeoLocationAlertDialogDelegate(private val city: String,
                                     private val location: Location,
                                     private val clickListener: AlertDialogClickListener) {

    private lateinit var alertDialog: AlertDialog

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
        alertDialog = builder.show()
    }

    fun dismissAlertDialog() {
        alertDialog.dismiss()
    }

    private fun positiveButtonClick(dialogInterface: DialogInterface) {
        clickListener.onPositiveClick(city, location)
        dialogInterface.dismiss()
    }

    private fun negativeButtonClick(dialogInterface: DialogInterface) {
        clickListener.onNegativeClick()
        dialogInterface.dismiss()
    }
}