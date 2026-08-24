package io.github.vladchenko.weatherforecast.core.ui.status

/**
 * Data class representing UI status information.
 *
 * @property message Human-readable status message to display to the user.
 * @property messageColor Color resource or ARGB integer for rendering the status message text.
 */
data class StatusData(
    val message: String,
    val messageColor: Int
)