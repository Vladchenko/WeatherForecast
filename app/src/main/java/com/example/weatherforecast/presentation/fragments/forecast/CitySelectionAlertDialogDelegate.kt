package com.example.weatherforecast.presentation.fragments.forecast

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.presentation.AlertDialogClickListener

/**
 * Shows alert dialog and processes its buttons clicks
 */
class CitySelectionAlertDialogDelegate(private val city: String,
                                       private val clickListener: AlertDialogClickListener) {

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
        clickListener.onPositiveClick(city, null)
        dialogInterface.dismiss()
    }

    private fun negativeButtonClick(dialogInterface: DialogInterface) {
        clickListener.onNegativeClick()
        dialogInterface.dismiss()
    }
}