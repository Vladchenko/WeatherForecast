package com.example.weatherforecast.presentation.alertdialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.R
import com.example.weatherforecast.utils.ResourceManager

/**
 * Shows alert dialog when geo location permission is not granted
 *
 * @property resourceManager to get strings from resources
 * @property onPositiveClick positive click callback
 * @property onNegativeClick negative click callback
 */
class LocationPermissionAlertDialogDelegate(
    private val resourceManager: ResourceManager,
    private val onPositiveClick: () -> Unit,
    private val onNegativeClick: () -> Unit
) {

    /**
     * Get [AlertDialog.Builder] using [Context]
     */
    fun getAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(resourceManager.getString(R.string.current_location_denied))
        builder.setMessage(resourceManager.getString(R.string.geo_location_permission_offer))
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
        onPositiveClick()
        dialogInterface.dismiss()
    }

    private fun negativeButtonClick(dialogInterface: DialogInterface) {
        onNegativeClick()
        dialogInterface.dismiss()
    }
}