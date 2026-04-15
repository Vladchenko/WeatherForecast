package io.github.vladchenko.weatherforecast.core.ui.dialog

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating standard alert dialogs.
 *
 * Provides reusable, business-logic-independent methods to build common dialog types
 * with consistent styling and behavior across the application.
 */
@Singleton
class AlertDialogFactory @Inject constructor() {

    /**
     * Creates a basic confirmation dialog with title, message, and positive/negative actions.
     *
     * @param title Dialog title text
     * @param message Main content message
     * @param onPositive Callback triggered when the positive button is clicked
     * @param onNegative Optional callback for negative (cancel/dismiss) action
     * @return Configured [AlertDialogDelegate] instance ready to be shown
     */
    fun createBasicDialog(
        title: String,
        message: String,
        onPositive: () -> Unit,
        onNegative: (() -> Unit)? = null
    ): AlertDialogDelegate {
        return BasicAlertDialogDelegate(
            title = title,
            message = message,
            onPositive = onPositive,
            onNegative = onNegative
        )
    }

    /**
     * Creates a dialog with custom button texts using string resource IDs.
     *
     * @param title Dialog title text
     * @param message Main content message
     * @param positiveButtonTextRes Resource ID for the positive button text
     * @param negativeButtonTextRes Optional resource ID for the negative button text; defaults to "Cancel" if null
     * @param onPositive Callback triggered when the positive button is clicked
     * @param onNegative Optional callback for negative action
     * @return Configured [AlertDialogDelegate] instance
     */
    fun createCustomButtonsDialog(
        title: String,
        message: String,
        positiveButtonTextRes: Int,
        negativeButtonTextRes: Int? = null,
        onPositive: () -> Unit,
        onNegative: (() -> Unit)? = null
    ): AlertDialogDelegate {
        return BasicAlertDialogDelegate(
            title = title,
            message = message,
            onPositive = onPositive,
            onNegative = onNegative,
            positiveButtonTextRes = positiveButtonTextRes,
            negativeButtonTextRes = negativeButtonTextRes ?: android.R.string.cancel
        )
    }

    /**
     * Creates an informational dialog with a single confirmation button (e.g., "OK").
     *
     * Useful for displaying non-critical messages or alerts that require user acknowledgment.
     *
     * @param title Dialog title text
     * @param message Content message to display
     * @param onConfirm Action to perform when the user confirms (e.g., closes the dialog)
     * @return Ready-to-show [AlertDialogDelegate] with only a positive action
     */
    fun createInfoDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ): AlertDialogDelegate {
        return BasicAlertDialogDelegate(
            title = title,
            message = message,
            onPositive = onConfirm,
            onNegative = null
        )
    }
}