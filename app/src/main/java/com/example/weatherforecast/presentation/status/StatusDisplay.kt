package com.example.weatherforecast.presentation.status

import com.example.weatherforecast.models.presentation.MessageType

/**
 * Interface for components that can display status messages to the user.
 * Implementations decide how to present the status (UI, logs, etc.)
 */
interface StatusDisplay {

    /**
     * Shows a status message
     *
     * @param status message to show
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