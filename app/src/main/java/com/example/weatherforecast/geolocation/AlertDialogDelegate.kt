package com.example.weatherforecast.geolocation

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import java.lang.ref.WeakReference

/**
 * Shows alert dialog and processes its buttons clicks
 */
class AlertDialogDelegate(private val city: String,
                          private val clickListener: AlertDialogClickListener) {

    private lateinit var alertDialog: AlertDialog

    /**
     * Show alert dialog using [android.content.Context]
     */
    fun showAlertDialog(weakReference: WeakReference<Activity>) {
        val builder = AlertDialog.Builder(weakReference.get() as Context)
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
        clickListener.onPositiveClick(city)
        dialogInterface.dismiss()
    }

    private fun negativeButtonClick(dialogInterface: DialogInterface) {
        clickListener.onNegativeClick()
        dialogInterface.dismiss()
    }
}