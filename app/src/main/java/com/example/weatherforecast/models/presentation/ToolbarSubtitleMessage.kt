package com.example.weatherforecast.models.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

/**
 * Data model for the toolbar subtitle
 *
 * @property stringId to be defined from strings.xml
 * @property valueForStringId to be used for stringId as, say %1s
 * @property color for subtitle
 * @property messageType to define what color to use for this message
 */
data class ToolbarSubtitleMessage(
    @StringRes val stringId: Int?,
    val valueForStringId: String?,
    val color: Color,
    val messageType: MessageType
)
