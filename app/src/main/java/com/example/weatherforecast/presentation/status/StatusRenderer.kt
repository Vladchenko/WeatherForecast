package com.example.weatherforecast.presentation.status

import com.example.weatherforecast.R
import com.example.weatherforecast.models.presentation.Message
import com.example.weatherforecast.models.presentation.MessageType
import com.example.weatherforecast.utils.ResourceManager

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
 * @property statusDisplay The target component that will display the status (e.g., AppBarViewModel)
 * @property resourceManager Helper for resolving Android string resources
 */
class StatusRenderer(
    private val statusDisplay: StatusDisplay,
    private val resourceManager: ResourceManager
) {

    /**
     * Updates the displayed status based on a [Message] object.
     *
     * Translates message content (text or resource ID) into a visible UI status
     * with appropriate type (info, warning, error). Uses [resourceManager] to resolve string resources.
     *
     * @param message The message containing status text/content and type
     */
    fun updateFromMessage(message: Message) {
        val status = when (message) {
            is Message.Success -> {
                when (val content = message.content) {
                    is Message.Content.Text ->
                        StatusDisplay.Status(text = content.message, type = MessageType.INFO)

                    is Message.Content.Resource ->
                        StatusDisplay.Status(
                            text = resourceManager.getString(content.resId, *content.args),
                            type = MessageType.INFO
                        )
                }
            }

            is Message.Error -> {
                when (val content = message.content) {
                    is Message.Content.Text ->
                        StatusDisplay.Status(text = content.message, type = MessageType.ERROR)

                    is Message.Content.Resource ->
                        StatusDisplay.Status(
                            text = resourceManager.getString(content.resId, *content.args),
                            type = MessageType.ERROR
                        )
                }
            }

            is Message.Warning -> {
                when (val content = message.content) {
                    is Message.Content.Text ->
                        StatusDisplay.Status(text = content.message, type = MessageType.WARNING)

                    is Message.Content.Resource ->
                        StatusDisplay.Status(
                            text = resourceManager.getString(content.resId, *content.args),
                            type = MessageType.WARNING
                        )
                }
            }
        }
        statusDisplay.showStatus(status)
    }

    /**
     * Displays a simple info status message.
     *
     * @param text Message text to show in the UI
     */
    fun showStatus(text: String) {
        statusDisplay.showStatus(StatusDisplay.Status(text = text, type = MessageType.INFO))
    }

    /**
     * Displays an error status message.
     *
     * @param text Error message text to show in the UI
     */
    fun showError(text: String) {
        statusDisplay.showStatus(StatusDisplay.Status(text = text, type = MessageType.ERROR))
    }

    /**
     * Displays a warning status message.
     *
     * @param text Warning message text to show in the UI
     */
    fun showWarning(text: String) {
        statusDisplay.showStatus(StatusDisplay.Status(text = text, type = MessageType.WARNING))
    }

    /**
     * Shows a default status indicating that city selection is active.
     *
     * Loads the message from [R.string.city_selection_title].
     */
    fun showCitySelectionStatus() {
        statusDisplay.showStatus(
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
     * Otherwise, formats message using [R.string.forecast_for_city_loading] with city name.
     *
     * @param city Name of the city being loaded
     */
    fun showDownloadingStatusFor(city: String) {
        if (city.isBlank()) {
            statusDisplay.showStatus(
                StatusDisplay.Status(
                    text = resourceManager.getString(R.string.forecast_downloading),
                    type = MessageType.INFO
                )
            )
        } else {
            statusDisplay.showStatus(
                StatusDisplay.Status(
                    text = resourceManager.getString(
                        R.string.forecast_for_city_loading,
                        city
                    ),
                    type = MessageType.INFO
                )
            )
        }
    }

    /**
     * Factory for creating [StatusRenderer] with ViewModel (not available in DI graph).
     * Provided via [com.example.weatherforecast.di.PresentationModule].
     */
    class Factory(private val resourceManager: ResourceManager) {
        fun create(statusTarget: StatusDisplay): StatusRenderer =
            StatusRenderer(statusTarget, resourceManager)
    }
}