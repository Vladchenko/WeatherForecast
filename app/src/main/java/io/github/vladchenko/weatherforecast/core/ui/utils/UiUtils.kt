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
     * URL-encodes this string using UTF-8 encoding.
     *
     * Replaces spaces with `%20` instead of `+` for proper URL encoding.
     * Useful for encoding city names and other parameters in URLs.
     *
     * Example: `"New York, NY"` → `"New%20York,%20NY"`
     */
    fun String.urlEncode(): String =
        java.net.URLEncoder.encode(this, "UTF-8").replace("+", "%20")

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

    /**
     * Returns the appropriate weather background drawable based on [weatherType] description.
     *
     * Supports OpenWeatherMap condition descriptions (case-insensitive).
     * Falls back to clear-sky background if no match is found.
     */
    fun getWeatherBackgroundResource(weatherType: String): Int {
        val type = weatherType.lowercase()

        return when {
            // Thunderstorm variations
            type.contains("thunderstorm with light rain") ||
                    type.contains("thunderstorm with rain") ||
                    type.contains("thunderstorm with heavy rain") ||
                    type.contains("light thunderstorm") ||
                    type.contains("thunderstorm") ||
                    type.contains("heavy thunderstorm") ||
                    type.contains("ragged thunderstorm") ||
                    type.contains("thunderstorm with light drizzle") ||
                    type.contains("thunderstorm with drizzle") ||
                    type.contains("thunderstorm with heavy drizzle") -> R.drawable.weather_bg_thunderstorm

            // Drizzle variations
            type.contains("light intensity drizzle") ||
                    type.contains("drizzle") ||
                    type.contains("heavy intensity drizzle") ||
                    type.contains("light intensity drizzle rain") ||
                    type.contains("drizzle rain") ||
                    type.contains("heavy intensity drizzle rain") ||
                    type.contains("shower rain and drizzle") ||
                    type.contains("heavy shower rain and drizzle") ||
                    type.contains("shower drizzle") -> R.drawable.weather_bg_drizzle

            // Rain variations
            type.contains("light rain") ||
                    type.contains("moderate rain") ||
                    type.contains("rain") ||
                    type.contains("heavy intensity rain") ||
                    type.contains("very heavy rain") ||
                    type.contains("extreme rain") ||
                    type.contains("freezing rain") ||
                    type.contains("light intensity shower rain") ||
                    type.contains("shower rain") ||
                    type.contains("heavy intensity shower rain") ||
                    type.contains("ragged shower rain") -> R.drawable.weather_bg_rain

            // Snow variations
            type.contains("light snow") ||
                    type.contains("snow") ||
                    type.contains("heavy snow") ||
                    type.contains("sleet") ||
                    type.contains("light shower sleet") ||
                    type.contains("shower sleet") ||
                    type.contains("light rain and snow") ||
                    type.contains("rain and snow") ||
                    type.contains("light shower snow") ||
                    type.contains("shower snow") ||
                    type.contains("heavy shower snow") -> R.drawable.weather_bg_snow

            // Mist, fog, haze, smoke
            type.contains("mist") ||
                    type.contains("fog") ||
                    type.contains("haze") ||
                    type.contains("smoke") -> R.drawable.weather_bg_mist

            // Sand, dust, volcanic ash
            type.contains("sand/dust whirls") ||
                    type.contains("sand") ||
                    type.contains("dust") ||
                    type.contains("volcanic ash") -> R.drawable.weather_bg_sand

            // Squalls, tornado
            type.contains("squalls") ||
                    type.contains("tornado") -> R.drawable.weather_bg_tornado

            // Clear sky
            type.contains("clear sky") -> R.drawable.weather_bg_clearsky

            // Clouds variations
            type.contains("few clouds") ||
                    type.contains("scattered clouds") ||
                    type.contains("broken clouds") ||
                    type.contains("overcast clouds") ||
                    type.contains("clouds") -> R.drawable.weather_bg_clouds

            else -> R.drawable.weather_bg_clearsky
        }
    }
}