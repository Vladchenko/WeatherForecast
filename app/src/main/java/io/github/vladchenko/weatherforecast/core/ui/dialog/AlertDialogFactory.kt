package io.github.vladchenko.weatherforecast.core.ui.dialog

import androidx.annotation.StringRes
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
     * @param args Arguments for string formatting
     * @param titleResId Dialog title text string resource id
     * @param messageResId Main content message string resource id
     * @param onPositive Callback triggered when the positive button is clicked
     * @param onNegative Optional callback for negative (cancel/dismiss) action
     * @return Configured [AlertDialogDelegate] instance ready to be shown
     */
    fun createBasicDialog(
        vararg args: Any,
        @StringRes titleResId: Int,
        @StringRes messageResId: Int,
        onPositive: () -> Unit,
        onNegative: (() -> Unit)? = null
    ): AlertDialogDelegate {
        return BasicAlertDialogDelegate(
            args = args,
            titleResId = titleResId,
            messageResId = messageResId,
            onPositive = onPositive,
            onNegative = onNegative
        )
    }

    /**
     * Creates a dialog with custom button texts using string resource IDs.
     *
     * @param titleResId Dialog title text
     * @param messageResId Main content message
     * @param positiveButtonTextRes Resource ID for the positive button text
     * @param negativeButtonTextRes Optional resource ID for the negative button text; defaults to "Cancel" if null
     * @param onPositive Callback triggered when the positive button is clicked
     * @param onNegative Optional callback for negative action
     * @return Configured [AlertDialogDelegate] instance
     */
    fun createCustomButtonsDialog(
        vararg args: Any,
        @StringRes titleResId: Int,
        @StringRes messageResId: Int,
        positiveButtonTextRes: Int,
        negativeButtonTextRes: Int? = null,
        onPositive: () -> Unit,
        onNegative: (() -> Unit)? = null
    ): AlertDialogDelegate {
        return BasicAlertDialogDelegate(
            args = args,
            onPositive = onPositive,
            onNegative = onNegative,
            titleResId = titleResId,
            messageResId = messageResId,
            positiveButtonTextRes = positiveButtonTextRes,
            negativeButtonTextRes = negativeButtonTextRes ?: android.R.string.cancel
        )
    }

    /**
     * Creates an informational dialog with a single confirmation button (e.g., "OK").
     *
     * Useful for displaying non-critical messages or alerts that require user acknowledgment.
     *
     * @param titleResId Dialog title text string resource ID
     * @param messageResId Content message text string resource ID
     * @param onConfirm Action to perform when the user confirms (e.g., closes the dialog)
     * @return Ready-to-show [AlertDialogDelegate] with only a positive action
     */
    fun createInfoDialog(
        vararg args: Any,
        onConfirm: () -> Unit,
        @StringRes messageResId: Int,
        @StringRes titleResId: Int
    ): AlertDialogDelegate {
        return BasicAlertDialogDelegate(
            args = args,
            messageResId = messageResId,
            titleResId = titleResId,
            onPositive = onConfirm,
            onNegative = null
        )
    }
}