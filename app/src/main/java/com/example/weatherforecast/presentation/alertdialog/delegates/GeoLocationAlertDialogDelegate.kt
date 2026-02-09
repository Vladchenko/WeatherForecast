package com.example.weatherforecast.presentation.alertdialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.R
import com.example.weatherforecast.utils.ResourceManager

/**
 * Alert dialog that asks user if the geo located city is a proper one to provide a weather forecast on.
 * If a user agrees to the located city, its forecast is downloaded, otherwise, user is suggested
 * to choose another city.
 *
 * @property city to point what city geo location has defined.
 * @property resourceManager to get strings from resources
 * @property onPositiveClick ok button click callback
 * @property onNegativeClick cancel button click callback
 */
class GeoLocationAlertDialogDelegate(private val city: String,
                                     private val resourceManager: ResourceManager,
                                     private val onPositiveClick: (String) -> Unit,
                                     private val onNegativeClick: () -> Unit) {

    /**
     * Get [AlertDialog.Builder] using [Context]
     */
    fun getAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(resourceManager.getString(R.string.geo_location_title))
        builder.setMessage(resourceManager.getString(R.string.geo_location_message))
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