package com.example.weatherforecast.presentation

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.compose.ui.graphics.Color
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_DEFAULT_FONT_SIZE
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_SMALL_FONT_SIZE

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
     * Defining weather type icon by [weatherIconId].
     * Codes present at https://openweathermap.org/weather-conditions#Icon-list
     */
    fun getWeatherTypeIcon(weatherIconId: String): Int {
        val resourceId = weatherIconMap[weatherIconId] ?: 0
        return if (resourceId > 0) {
            resourceId
        } else {
            R.drawable.ic_0
        }
    }

    private val weatherIconMap by lazy {
        mapOf(
            "01d" to R.drawable.ic_01d,
            "01n" to R.drawable.ic_01n,
            "02d" to R.drawable.ic_02d,
            "02n" to R.drawable.ic_02n,
            "03d" to R.drawable.ic_03d,
            "03n" to R.drawable.ic_03n,
            "04d" to R.drawable.ic_04d,
            "04n" to R.drawable.ic_04n,
            "09d" to R.drawable.ic_09d,
            "09n" to R.drawable.ic_09n,
            "10d" to R.drawable.ic_10d,
            "10n" to R.drawable.ic_10n,
            "11d" to R.drawable.ic_11d,
            "11n" to R.drawable.ic_11n,
            "13d" to R.drawable.ic_13d,
            "13n" to R.drawable.ic_13n,
            "50d" to R.drawable.ic_50d,
            "50n" to R.drawable.ic_50n,
        )
    }

    fun Context.resolveColorAttr(@AttrRes attr: Int): Color {
        return TypedValue().run {
            theme.resolveAttribute(attr, this, true)
            Color(this.data)
        }
    }

    const val SHARED_PREFERENCES_KEY = "Shared preferences key"
}