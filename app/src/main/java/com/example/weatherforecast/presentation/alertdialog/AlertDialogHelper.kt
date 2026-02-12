package com.example.weatherforecast.presentation.alertdialog

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.weatherforecast.presentation.alertdialog.delegates.AlertDialogDelegate

/**
 * Helper for creating consistently styled alert dialogs.
 *
 * Configures common dialog properties
 *
 * @constructor
 * @property context android.context.Context
 */
class AlertDialogHelper(private val context: Context) {

    /**
     * Creates an alert dialog from a pre-configured [AlertDialog.Builder].
     *
     * Applies standard app-wide settings:
     * - Non-cancelable (back button and touch outside disabled)
     * - Debug log on creation
     *
     * @param alertDialogDelegate A fully configured builder (typically from a delegate)
     * @return A ready-to-show [AlertDialog] instance with enforced behavior
     */
    fun getAlertDialogBuilder(alertDialogDelegate: AlertDialogDelegate): AlertDialog {
        val alertDialogBuilder = alertDialogDelegate.getAlertDialogBuilder(context)
        Log.d(TAG, "Create alertDialog by its delegate")
        val dialog = alertDialogBuilder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    companion object {
        private const val TAG = "AlertDialogHelper"
    }
}