package com.example.weatherforecast.presentation.fragments

import android.content.res.Resources
import androidx.appcompat.widget.Toolbar
import com.example.weatherforecast.R

/**
 *
 */
object PresentationUtils {

    /**
     *
     */
    fun setToolbarSubtitleFontSize(toolbar: Toolbar, subtitle: String) {
        if (subtitle.length > 50) {
            toolbar.setSubtitleTextAppearance(toolbar.context, R.style.ToolbarSubtitleSmallAppearance)
        } else {
            toolbar.setSubtitleTextAppearance(toolbar.context, R.style.ToolbarSubtitleMediumAppearance)
        }
    }

    /**
     *
     */
    fun getWeatherTypeIcon(resources: Resources, packageName: String, weatherType: String) =
        resources.getIdentifier(
            ICON_PREFIX + weatherType.replace(" ", ""),
            DRAWABLE_RESOURCE_TYPE,
            packageName
        )

    private const val ICON_PREFIX = "icon_"
    private const val DRAWABLE_RESOURCE_TYPE = "drawable"
}