package io.github.vladchenko.weatherforecast.core.ui.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog

/**
 * Basic implementation of [AlertDialogDelegate] with title, message, and positive/negative buttons.
 *
 * Provides a standardized dialog configuration with:
 * - Configurable title and message
 * - Mandatory positive action (e.g., "OK")
 * - Optional negative action (e.g., "Cancel")
 * - Customizable button texts via Android resource IDs
 * - Control over dismiss behavior (back press / touch outside)
 *
 * @property title Dialog title text
 * @property message Dialog content message
 * @property onPositive Callback triggered when the positive button is clicked
 * @property onNegative Optional callback for negative (cancel/dismiss) action
 * @property positiveButtonTextRes Resource ID for positive button text; defaults to [android.R.string.ok]
 * @property negativeButtonTextRes Resource ID for negative button text; defaults to [android.R.string.cancel]
 * @property isCancelable Whether the dialog can be dismissed by pressing back or tapping outside; defaults to `false`
 */
class BasicAlertDialogDelegate(
    private val title: String,
    private val message: String,
    private val onPositive: () -> Unit,
    private val onNegative: (() -> Unit)? = null,
    private val positiveButtonTextRes: Int = android.R.string.ok,
    private val negativeButtonTextRes: Int = android.R.string.cancel,
    private val isCancelable: Boolean = false
) : AlertDialogDelegate {

    /**
     * Creates and configures an [AlertDialog.Builder] using the provided context.
     *
     * Applies all properties including title, message, buttons with their respective callbacks,
     * and cancelability settings. The returned builder can be used to create the final [AlertDialog].
     *
     * @param context Context used to build the dialog
     * @return Configured [AlertDialog.Builder] instance
     */
    override fun createAlertDialogBuilder(context: Context): AlertDialog.Builder {
        return AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveButtonTextRes) { dialog, _ ->
                onPositive()
                dialog.dismiss()
            }
            onNegative?.let {
                setNegativeButton(negativeButtonTextRes) { dialog, _ ->
                    it()
                    dialog.dismiss()
                }
            }
            setCancelable(isCancelable)
        }
    }
}