package io.github.vladchenko.weatherforecast.core.ui.status

import androidx.annotation.AttrRes

/**
 * Data class representing UI status information.
 *
 * @property message Text content to display, which can be either a plain string ([TextType.Text])
 *   or a string resource ID with optional formatting arguments ([TextType.ResId]).
 * @property messageColorAttr Color resource or ARGB integer for rendering the status message text.
 */
data class StatusData(
    val message: TextType,
    @param:AttrRes val messageColorAttr: Int
)