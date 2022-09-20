package com.example.weatherforecast.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Resources
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnDetach
import com.example.weatherforecast.R

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
     * Weather type icon retrieving from [resources], having a path to this icon, i.e. [packageName] and weather
     * description, i.e. [weatherType] specified.
     */
    fun getWeatherTypeIcon(resources: Resources, packageName: String, weatherType: String) =
        resources.getIdentifier(
            ICON_PREFIX + weatherType.replace(" ", ""),
            DRAWABLE_RESOURCE_TYPE,
            packageName
        )

    /**
     * [view]'s fading out animating within a [shortAnimationDuration] time span.
     */
    fun animateFadeOut(view: View, shortAnimationDuration: Int) {
        view.apply {
            animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                    }
                })
        }
    }

    fun AlertDialog.closeWith(view:View) {
        view.doOnDetach { this.cancel() }
    }

    const val SHARED_PREFERENCES_KEY = "Shared preferences key"
    private const val ICON_PREFIX = "icon_"
    private const val DRAWABLE_RESOURCE_TYPE = "drawable"
}