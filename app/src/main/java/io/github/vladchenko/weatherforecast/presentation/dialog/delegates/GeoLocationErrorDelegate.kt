package io.github.vladchenko.weatherforecast.presentation.dialog.delegates

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogDelegate

/**
 * Alert dialog informs user that geo location service failed.
 *
 * @property onPositiveClick ok button click callback
 * @property onNegativeClick cancel button click callback
 */
class GeoLocationErrorDelegate(
    private val onPositiveClick: () -> Unit,
    private val onNegativeClick: () -> Unit
) : AlertDialogDelegate {

    /**
     * Get [AlertDialog.Builder] using [Context]
     */
    override fun createAlertDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.geo_fail_title))
        builder.setMessage(context.getString(R.string.geo_fail_subtitle))
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