package com.example.weatherforecast.presentation.alertdialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.R
import com.example.weatherforecast.utils.ResourceManager

/**
 * Alert dialog informs user that geo location service failed.
 *
 * @property resourceManager to get strings from resources
 * @property onPositiveClick ok button click callback
 * @property onNegativeClick cancel button click callback
 */
class GeoLocationErrorDelegate(
    private val resourceManager: ResourceManager,
    private val onPositiveClick: () -> Unit,
    private val onNegativeClick: () -> Unit
) : AlertDialogDelegate {

    /**
     * Get [AlertDialog.Builder] using [Context]
     */
    override fun createAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(resourceManager.getString(R.string.geo_fail_title))
        builder.setMessage(resourceManager.getString(R.string.geo_fail_subtitle))
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