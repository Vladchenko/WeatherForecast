package com.example.weatherforecast.presentation.alertdialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.R
import com.example.weatherforecast.utils.ResourceManager

/**
 * Shows an alert dialog when the selected city is not found during a server lookup.
 *
 * The dialog informs the user that no forecast data is available for the given city
 * and allows them to confirm or cancel the action.
 *
 * @property city Name of the city for which forecast was not found
 * @property resourceManager Helper to retrieve localized strings from resources
 * @property onPositiveClick Callback triggered when the "OK" button is pressed
 * @property onNegativeClick Callback triggered when the "Cancel" button is pressed (not currently set in builder)
 */
class SelectedCityNotFoundAlertDialogDelegate(
    private val city: String,
    private val resourceManager: ResourceManager,
    private val onPositiveClick: () -> Unit,
    private val onNegativeClick: () -> Unit
) : AlertDialogDelegate {

    /**
     * Creates and configures an [AlertDialog.Builder] for displaying the city not found dialog.
     *
     * Sets title, message, and positive button with dismissal behavior.
     * Negative button must be added separately if needed.
     *
     * @param context Android context required to build the dialog
     * @return Configured [AlertDialog.Builder] instance
     */
    override fun getAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(resourceManager.getString(R.string.forecast_no_data_for_city, city))
        builder.setMessage(resourceManager.getString(R.string.forecast_no_data_message))
        builder.setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
            positiveButtonClick(
                dialogInterface
            )
        }
        return builder
    }

    /**
     * Handles click on the positive (OK) button: executes the [onPositiveClick] callback
     * and dismisses the dialog.
     *
     * @param dialogInterface Reference to the dialog being dismissed
     */
    private fun positiveButtonClick(dialogInterface: DialogInterface) {
        onPositiveClick()
        dialogInterface.dismiss()
    }

    /**
     * Handles click on the negative (Cancel) button: executes the [onNegativeClick] callback
     * and dismisses the dialog.
     *
     * @param dialogInterface Reference to the dialog being dismissed
     */
    private fun negativeButtonClick(dialogInterface: DialogInterface) {
        onNegativeClick()
        dialogInterface.dismiss()
    }
}