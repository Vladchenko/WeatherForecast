package com.example.weatherforecast.presentation.alertdialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.R
import com.example.weatherforecast.utils.ResourceManager

/**
 * Shows an alert dialog when location permission is denied.
 *
 * Informs the user that location access is required and offers options to grant permission
 * or cancel and use manual city input instead.
 *
 * @property resourceManager Helper to retrieve localized strings from resources
 * @property onPositiveClick Callback triggered when the user taps "OK" (typically opens settings)
 * @property onNegativeClick Callback triggered when the user taps "Cancel"
 */
class LocationPermissionAlertDialogDelegate(
    private val resourceManager: ResourceManager,
    private val onPositiveClick: () -> Unit,
    private val onNegativeClick: () -> Unit
): AlertDialogDelegate {

    /**
     * Creates and configures an [AlertDialog.Builder] for the location permission denial dialog.
     *
     * Sets title, message, and both positive ("OK") and negative ("Cancel") buttons
     * with appropriate click listeners.
     *
     * @param context Android context required to build the dialog
     * @return Fully configured [AlertDialog.Builder] instance
     */
    override fun getAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(resourceManager.getString(R.string.geo_permission_denied))
        builder.setMessage(resourceManager.getString(R.string.geo_permission_request_message))
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