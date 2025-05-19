package com.example.weatherforecast.presentation

import androidx.compose.ui.graphics.Color

/**
 * Constant values for presentation layer
 */
object PresentationConstants {
    /**
     * Appbar subtitle's font size for short subtitle length
     */
    const val APPBAR_SUBTITLE_DEFAULT_FONT_SIZE = 14

    /**
     * Appbar subtitle's font size for long subtitle length. Size rather small to fit screen.
     */
    const val APPBAR_SUBTITLE_SMALL_FONT_SIZE = 12

    val ERROR_STATUS_COLOR = Color.Red
    val WARNING_STATUS_COLOR = Color.Yellow
    val SUCCESS_STATUS_COLOR = Color.Black
}