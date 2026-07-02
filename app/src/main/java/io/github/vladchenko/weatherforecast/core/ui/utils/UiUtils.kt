package io.github.vladchenko.weatherforecast.core.ui.utils

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.ui.constants.SubtitleSize
import io.github.vladchenko.weatherforecast.core.ui.constants.UiConstants
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.resolveColorAttr

/**
 * Presentation layer utility methods.
 */
object UiUtils {

    /**
     * Composes a full city name from [cityName], [stateName] (if present), and [countryName].
     *
     * Examples: `"London, UK"`, `"New York, NY, USA"`, `"Paris"`.
     */
    fun formatFullCityName(cityName: String, stateName: String?, countryName: String) =
        if (stateName.isNullOrBlank()) {
            if (countryName.isNotBlank()) {
                "$cityName, $countryName"
            } else {
                cityName
            }
        } else {
            "$cityName, $stateName, $countryName"
        }

    /**
     * Returns the appropriate toolbar subtitle font size for this [SubtitleSize].
     *
     * Maps:
     * - [Small] -> [io.github.vladchenko.weatherforecast.core.ui.constants.UiConstants.APPBAR_SUBTITLE_SMALL_FONT_SIZE]
     * - [Normal] & [Large] -> [io.github.vladchenko.weatherforecast.core.ui.constants.UiConstants.APPBAR_SUBTITLE_DEFAULT_FONT_SIZE]
     */
    fun SubtitleSize.toToolbarSubtitleFontSize(): TextUnit {
        return when (this) {
            SubtitleSize.Small -> UiConstants.APPBAR_SUBTITLE_SMALL_FONT_SIZE
            SubtitleSize.Normal -> UiConstants.APPBAR_SUBTITLE_DEFAULT_FONT_SIZE
            SubtitleSize.Large -> UiConstants.APPBAR_SUBTITLE_DEFAULT_FONT_SIZE
        }
    }

    /**
     * Returns the appropriate weather icon drawable resource for [weatherIconId].
     *
     * Supports OpenWeatherMap icon codes (e.g., `"10d"`, `"50n"`).
     * Falls back to `R.drawable.ic_0` if unknown.
     * Codes present at https://openweathermap.org/weather-conditions#Icon-list
     */
    fun toWeatherIconRes(weatherIconId: String): Int =
        weatherIconMap[weatherIconId] ?: R.drawable.ic_0

    private val weatherIconMap =
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

    /**
     * Resolves a theme attribute color (e.g., `?attr/colorPrimary`) into a Compose [Color].
     *
     * Useful for integrating XML-based theme attributes into Compose UI.
     */
    fun Context.resolveColorAttr(@AttrRes attrRes: Int): Color {
        val typedValue = TypedValue()
        val resolveSuccess = theme.resolveAttribute(attrRes, typedValue, true)
        return if (resolveSuccess) {
            Color(typedValue.data)
        } else {
            // Fallback: если атрибут не найден — возвращаем Color.Unspecified
            Color.Unspecified
        }
    }

    /**
     * A [remember]-enabled version of [resolveColorAttr] for Composables.
     *
     * Caches the resolved color for [attrRes] across recompositions.
     */
    @Composable
    fun rememberResolvedColorAttr(@AttrRes attrRes: Int): Color {
        val context = LocalContext.current
        return remember(attrRes) {
            context.resolveColorAttr(attrRes)
        }
    }
}