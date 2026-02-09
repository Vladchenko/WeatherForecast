package com.example.weatherforecast.models.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

/**
 * Data model representing a message to be displayed in the toolbar subtitle.
 *
 * Encapsulates:
 * - A string resource ID for localization
 * - An optional value to format the string (e.g., city name in "Weather in %1s")
 * - Text color for visual styling
 * - Message type ([MessageType.INFO], [MessageType.WARNING], [MessageType.ERROR]) for semantic categorization
 *
 * Used by [AppBarViewModel] and [StatusRenderer] to dynamically update the app bar's subtitle
 * with localized, styled status messages.
 *
 * @property stringId Resource ID of the string in [strings.xml]; nullable for dynamic text
 * @property valueForStringId Optional value to format the string resource
 * @property color The color to apply to the subtitle text
 * @property messageType Semantic type of the message, used for consistent theming
 */
data class ToolbarSubtitleMessage(
    @StringRes val stringId: Int?,
    val valueForStringId: String?,
    val color: Color,
    val messageType: MessageType
)
