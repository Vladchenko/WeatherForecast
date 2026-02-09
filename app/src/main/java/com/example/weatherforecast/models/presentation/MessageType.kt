package com.example.weatherforecast.models.presentation

/**
 * Enumerates the types of messages that can be displayed in the application's UI,
 * particularly in the toolbar subtitle.
 *
 * Used to differentiate between informational, error, and warning states,
 * allowing for appropriate visual styling (e.g., color coding).
 *
 * @see ToolbarSubtitleMessage
 * @see com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
 * @see com.example.weatherforecast.presentation.status.StatusRenderer
 */
enum class MessageType {
    /**
     * Represents an informational message.
     *
     * Typically displayed in neutral or primary text color.
     */
    INFO,

    /**
     * Represents an error message.
     *
     * Indicates a failure or invalid state; usually styled with red or error color.
     */
    ERROR,

    /**
     * Represents a warning message.
     *
     * Indicates a non-critical issue; often styled with yellow or warning color.
     */
    WARNING
}