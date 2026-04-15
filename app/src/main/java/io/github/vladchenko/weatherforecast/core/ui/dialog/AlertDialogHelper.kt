package io.github.vladchenko.weatherforecast.core.ui.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for creating and displaying standardized alert dialogs across the application.
 *
 * Ensures consistent look, feel, and behavior by applying shared configuration such as:
 * - Non-cancelable by back button or touch outside
 * - Unified styling via [AlertDialogDelegate]
 *
 * Injected with application context to avoid memory leaks and support dialog creation from non-UI components.
 *
 * @property context Application context used to build dialogs
 */
@Singleton
class AlertDialogHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Creates an [AlertDialog] from the given [AlertDialogDelegate] with standard settings applied.
     *
     * The resulting dialog is:
     * - Not cancelable via back press (`setCancelable(false)`)
     * - Not dismissible by tapping outside (`setCanceledOnTouchOutside(false)`)
     *
     * @param delegate The delegate providing title, message, and action callbacks
     * @return Fully configured [AlertDialog] instance, ready to be shown
     *
     * @see AlertDialogDelegate for defining dialog content and behavior
     */
    fun createDialog(delegate: AlertDialogDelegate): AlertDialog {
        val alertDialogBuilder = delegate.createAlertDialogBuilder(context)
        val dialog = alertDialogBuilder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    /**
     * Creates and immediately displays an alert dialog using the provided delegate.
     *
     * Convenience method that combines dialog creation and display.
     *
     * @param delegate The delegate defining the dialog's content and actions
     *
     * @see createDialog for creating without immediate show
     */
    fun showDialog(delegate: AlertDialogDelegate) {
        createDialog(delegate).show()
    }
}