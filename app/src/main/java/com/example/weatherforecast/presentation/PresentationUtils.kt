package com.example.weatherforecast.presentation

import android.content.res.Resources
import com.example.weatherforecast.R
import com.example.weatherforecast.models.presentation.MessageType
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_DEFAULT_FONT_SIZE
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_SMALL_FONT_SIZE
import com.example.weatherforecast.presentation.PresentationConstants.ERROR_STATUS_COLOR
import com.example.weatherforecast.presentation.PresentationConstants.SUCCESS_STATUS_COLOR
import com.example.weatherforecast.presentation.PresentationConstants.WARNING_STATUS_COLOR

/**
 * Presentation layer utility methods.
 */
object PresentationUtils {

    /**
     * Compose a full name for a city, consisting of [cityName], [stateName] if present and [countryName].
     */
    fun getFullCityName(cityName: String, stateName: String?, countryName: String) =
        if (stateName.isNullOrBlank()) {
            "$cityName, $countryName"
        } else {
            "$cityName, $stateName, $countryName"
        }

    /**
     * Get font size for [subtitle] length.
     */
    fun getToolbarSubtitleFontSize(subtitle: String) =
        if (subtitle.length > 50) {
            APPBAR_SUBTITLE_SMALL_FONT_SIZE
        } else {
            APPBAR_SUBTITLE_DEFAULT_FONT_SIZE
        }

    /**
     * Defines a color for toolbar by its [type]
     */
    fun getToolbarSubtitleColor(type: MessageType) =
        when(type) {
            MessageType.INFO -> SUCCESS_STATUS_COLOR
            MessageType.WARNING -> WARNING_STATUS_COLOR
            MessageType.ERROR -> ERROR_STATUS_COLOR
        }

    /**
     * Weather type icon retrieving from [resources], having a path to this icon, i.e. [packageName]
     * and weather description, i.e. [weatherType] specified.
     */
    fun getWeatherTypeIcon(resources: Resources, packageName: String, weatherType: String): Int {
        val resourceId = resources.getIdentifier(
            ICON_PREFIX + weatherType.replace(" ", ""),
            DRAWABLE_RESOURCE_TYPE,
            packageName
        )
        return if (resourceId > 0) {
            resourceId
        } else {
            R.drawable.icon_clearsky
        }
    }

    private const val ICON_PREFIX = "icon_"
    private const val DRAWABLE_RESOURCE_TYPE = "drawable"
    const val SHARED_PREFERENCES_KEY = "Shared preferences key"
}