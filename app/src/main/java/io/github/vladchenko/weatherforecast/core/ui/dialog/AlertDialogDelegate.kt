package io.github.vladchenko.weatherforecast.core.ui.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog

/**
 * Interface defining a contract for creating and configuring alert dialogs.
 *
 * Implementations of this delegate are responsible for providing the logic to build an [AlertDialog]
 * with specific content, styling, and behavior. This abstraction enables dependency injection,
 * testing, and separation between dialog configuration and presentation.
 *
 * Used in combination with [AlertDialogHelper] to create and show dialogs consistently across the app.
 */
interface AlertDialogDelegate {

    /**
     * Creates and configures an [AlertDialog.Builder] using the provided context.
     *
     * The returned builder should be fully set up with:
     * - Title and message
     * - Positive/negative buttons and their click listeners
     * - Icon (optional)
     * - Cancelable behavior
     *
     * @param context The context used to instantiate the dialog; typically the current Activity or Application
     * @return A configured [AlertDialog.Builder] instance ready to create the final dialog
     */
    fun createAlertDialogBuilder(context: Context): AlertDialog.Builder
}