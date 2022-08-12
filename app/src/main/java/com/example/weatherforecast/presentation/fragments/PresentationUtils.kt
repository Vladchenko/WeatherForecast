package com.example.weatherforecast.presentation.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Resources
import android.view.View
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

    /**
     *
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

    private const val ICON_PREFIX = "icon_"
    private const val DRAWABLE_RESOURCE_TYPE = "drawable"
}