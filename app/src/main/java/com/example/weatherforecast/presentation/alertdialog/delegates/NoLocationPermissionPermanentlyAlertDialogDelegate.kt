package com.example.weatherforecast.presentation.alertdialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.R
import com.example.weatherforecast.utils.ResourceManager

/**
 * Shows an alert dialog when location permission has been permanently denied.
 *
 * Informs the user that location access is disabled and must be manually enabled
 * in system settings. Offers options to open settings or cancel and use manual input.
 *
 * @property resourceManager Helper to retrieve localized strings from resources
 * @property onPositiveClick Callback triggered when "OK" is tapped (typically opens app settings)
 * @property onNegativeClick Callback triggered when "Cancel" is tapped
 */
class NoLocationPermissionPermanentlyAlertDialogDelegate(
    private val resourceManager: ResourceManager,
    private val onPositiveClick: (String) -> Unit,
    private val onNegativeClick: () -> Unit
): AlertDialogDelegate {

    /**
     * Creates and returns a configured [AlertDialog.Builder] for this dialog.
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
        builder.setMessage(resourceManager.getString(R.string.geo_permission_denied_permanently))
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