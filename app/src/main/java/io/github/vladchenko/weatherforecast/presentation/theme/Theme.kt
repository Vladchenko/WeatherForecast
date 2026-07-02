package io.github.vladchenko.weatherforecast.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Core color definitions for the Weather Forecast app.
 *
 * Defines:
 * - [MainTextLight], [MainTextDark]: primary text colors (not used directly in onSurface due to intentional override)
 * - [PrimaryColor]: consistent brand accent across themes
 * - [BackgroundLight], [BackgroundDark]: theme-specific background colors
 *
 * Notably, [LightColorScheme] and [DarkColorScheme] intentionally set [onSurface] to [MainTextDark]
 * instead of a dimmer shade — to ensure high-contrast text on all surfaces.
 *
 * Use [WeatherForecastTheme] to apply theming in your Composables.
 */
val MainTextLight = Color(0xFF333333)
val MainTextDark = Color(0xFFFFFFFF)
val PrimaryColor = Color(0xFF6200EE)
val BackgroundLight = Color(0xFFFFFFFF)
val BackgroundDark = Color(0xFF121212)

/**
 * Light color palette.
 *
 * Sets [onSurface] to [MainTextDark] (pure white) to maximize contrast.
 */
val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onSurface = MainTextDark,
    background = BackgroundLight,
)

/**
 * Dark color palette.
 *
 * Sets [onSurface] to [MainTextDark] (pure white) — same as light theme — for consistent typography.
 */
val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onSurface = MainTextDark,
    background = BackgroundDark,
)

/**
 * Main app theme provider.
 *
 * Wraps [MaterialTheme] with the appropriate color scheme based on [darkTheme].
 * Defaults to system preference (via [isSystemInDarkTheme]), but allows manual override.
 *
 * Should be used as the top-level composable in the app hierarchy to enable consistent theming.
 *
 * @param darkTheme When `true`, uses dark theme; `false` — light theme.
 *                  Defaults to system setting.
 * @param content Content to be themed.
 */
@Composable
fun WeatherForecastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}