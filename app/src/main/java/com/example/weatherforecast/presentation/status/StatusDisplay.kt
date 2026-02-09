package com.example.weatherforecast.presentation.status

import com.example.weatherforecast.models.presentation.MessageType

/**
 * Interface for components that can display status messages to the user.
 * Implementations decide how to present the status (UI, logs, etc.)
 */
interface StatusDisplay {

    /**
     * Displays a status message in the app bar's subtitle.
     *
     * The appearance (text and color) depends on the message type:
     * - [MessageType.INFO]: Normal informational message
     * - [MessageType.WARNING]: Warning with appropriate color
     * - [MessageType.ERROR]: Error indication with error color
     *
     * @param status The status object containing message text and type
     */
    fun showStatus(status: Status)

    /**
     * Data class for status messages
     *
     * @property text message text
     * @property type message type (success, warning, etc.)
     */
    data class Status(
        val text: String,
        val type: MessageType
    )
}