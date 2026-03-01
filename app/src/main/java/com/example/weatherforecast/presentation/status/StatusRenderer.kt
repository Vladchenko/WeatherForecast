package com.example.weatherforecast.presentation.status

import com.example.weatherforecast.R
import com.example.weatherforecast.models.presentation.Message
import com.example.weatherforecast.models.presentation.MessageType
import com.example.weatherforecast.utils.ResourceManager
import javax.inject.Inject

/**
 * A utility class responsible for rendering application status messages in the UI.
 *
 * Translates various types of [Message] (e.g., success, error, warning) into visible statuses
 * via a [StatusDisplay] implementation, such as updating a subtitle in the app bar.
 *
 * Supports both text-based and resource-based messages, with proper string formatting.
 * Provides convenience methods for common status types like loading, city selection, and errors.
 *
 * This class is typically used in ViewModels to communicate transient UI states.
 *
 * @property currentTarget The target component that will display the status (e.g., AppBarViewModel)
 * @property resourceManager Helper for resolving Android string resources
 */
class StatusRenderer @Inject constructor(
    private val resourceManager: ResourceManager
) {

    private var currentTarget: StatusDisplay? = null

    /**
     * Sets the current component that will display status messages.
     *
     * Should be called when a UI component (e.g., AppBarViewModel) becomes active
     * and ready to receive status updates.
     *
     * @param target The status display implementation to use
     */
    fun setTarget(target: StatusDisplay) {
        this.currentTarget = target
    }

    /**
     * Clears the current status display target.
     *
     * Must be called when the previous target is being destroyed (e.g., in onCleared())
     * to prevent memory leaks and stale references.
     */
    fun clearTarget() {
        this.currentTarget = null
    }

    /**
     * Displays a simple info status message.
     *
     * @param text Message text to show in the UI
     */
    fun showStatus(text: String) {
        currentTarget?.showStatus(StatusDisplay.Status(text = text, type = MessageType.INFO))
    }

    /**
     * Displays an error status message.
     *
     * @param text Error message text to show in the UI
     */
    fun showError(text: String) {
        currentTarget?.showStatus(StatusDisplay.Status(text = text, type = MessageType.ERROR))
    }

    /**
     * Displays a warning status message.
     *
     * @param text Warning message text to show in the UI
     */
    fun showWarning(text: String) {
        currentTarget?.showStatus(StatusDisplay.Status(text = text, type = MessageType.WARNING))
    }

    /**
     * Shows a default status indicating that city selection is active.
     *
     * Loads the message from [R.string.city_selection_title].
     */
    fun showCitySelectionStatus() {
        currentTarget?.showStatus(
            StatusDisplay.Status(
                text = resourceManager.getString(R.string.city_selection_title),
                type = MessageType.INFO
            )
        )
    }

    /**
     * Shows a downloading/loading status for a specific city.
     *
     * If [city] is blank, shows generic loading message.
     * Otherwise, formats message using [R.string.forecast_loading] with city name.
     *
     * @param city Name of the city being loaded
     */
    fun showLoadingStatusFor(city: String) {
        if (city.isBlank()) {
            currentTarget?.showStatus(
                StatusDisplay.Status(
                    text = resourceManager.getString(R.string.forecast_downloading),
                    type = MessageType.INFO
                )
            )
        } else {
            currentTarget?.showStatus(
                StatusDisplay.Status(
                    text = resourceManager.getString(
                        R.string.forecast_loading,
                        city
                    ),
                    type = MessageType.INFO
                )
            )
        }
    }
}
