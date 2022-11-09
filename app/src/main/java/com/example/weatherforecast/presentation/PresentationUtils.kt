package com.example.weatherforecast.presentation

import android.content.res.Resources
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnDetach
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_DEFAULT_FONT_SIZE
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_SMALL_FONT_SIZE

/**
 * Presentation layer utility methods.
 */
object PresentationUtils {

    /**
     * Set font size for [toolbar], depending on its [subtitle] size.
     */
    fun setToolbarSubtitleFontSize(toolbar: Toolbar, subtitle: String) {
        if (subtitle.length > 50) {
            toolbar.setSubtitleTextAppearance(toolbar.context, R.style.ToolbarSubtitleSmallAppearance)
        } else {
            toolbar.setSubtitleTextAppearance(toolbar.context, R.style.ToolbarSubtitleMediumAppearance)
        }
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
     * Weather type icon retrieving from [resources], having a path to this icon, i.e. [packageName] and weather
     * description, i.e. [weatherType] specified.
     */
    fun getWeatherTypeIcon(resources: Resources, packageName: String, weatherType: String):Int {
        val resourceId = resources.getIdentifier(
            ICON_PREFIX + weatherType.replace(" ", ""),
            DRAWABLE_RESOURCE_TYPE,
            packageName
        )
        return if (resourceId > 0 ) {
            resourceId
        } else {
            resources.getIdentifier(
                "icon_clearsky",
                DRAWABLE_RESOURCE_TYPE,
                packageName
            )
        }
}

    fun AlertDialog.closeWith(view:View) {
        view.doOnDetach { this.cancel() }
    }

    const val REPEAT_INTERVAL = 5000L
    private const val ICON_PREFIX = "icon_"
    private const val DRAWABLE_RESOURCE_TYPE = "drawable"
    const val SHARED_PREFERENCES_KEY = "Shared preferences key"
}