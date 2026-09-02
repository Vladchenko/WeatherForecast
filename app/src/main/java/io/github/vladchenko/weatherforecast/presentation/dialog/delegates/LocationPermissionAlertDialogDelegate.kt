package io.github.vladchenko.weatherforecast.presentation.dialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogDelegate

/**
 * Shows an alert dialog when location permission is denied.
 *
 * Informs the user that location access is required and offers options to grant permission
 * or cancel and use manual city input instead.
 *
 * @property onPositiveClick Callback triggered when the user taps "OK" (typically opens settings)
 * @property onNegativeClick Callback triggered when the user taps "Cancel"
 */
class LocationPermissionAlertDialogDelegate(
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
    override fun createAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.geo_permission_denied)
        builder.setMessage(R.string.geo_permission_request_message)
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