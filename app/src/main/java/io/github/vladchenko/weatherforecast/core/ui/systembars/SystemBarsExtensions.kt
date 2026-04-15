package io.github.vladchenko.weatherforecast.core.ui.systembars

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.view.WindowCompat

/**
 * Utility class for managing the appearance of system bars (status bar and navigation bar).
 *
 * Allows:
 * - Making system bars transparent so that content can be drawn underneath.
 * - Controlling the status bar icon color (light or dark icons).
 *
 * Supports different Android versions according to Google's current guidelines.
 */

/**
 * Hides the system navigation bar (gesture or button-based) for a fullscreen experience.
 *
 * On Android 11 (API 30+) and higher, this method uses [android.view.WindowInsetsController]
 * to hide the navigation bar with sticky immersive behavior. The user can temporarily
 * reveal the bar by swiping from the bottom edge of the screen.
 *
 * On older Android versions, it falls back to the deprecated [android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION],
 * [android.view.View.SYSTEM_UI_FLAG_FULLSCREEN], and [android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY] flags.
 *
 * This method does not permanently disable navigation — users can always swipe
 * from the bottom to access back, home, and recent apps.
 *
 * @note Call this after content is set (e.g., in `onCreate` after `setContentView`).
 */
fun Activity.hideBottomNavigationBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val controller = window.insetsController ?: return
        controller.hide(WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }
}

/**
 * Sets transparent status and navigation bars, allowing content to render behind them.
 *
 * Applies the [android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS] flag,
 * sets both status bar and navigation bar colors to fully transparent (`0x00000000`),
 * and disables automatic window insets via [androidx.core.view.WindowCompat.setDecorFitsSystemWindows].
 *
 * Supported from API 21 (LOLLIPOP).
 */
fun Activity.setTransparentSystemBars() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            // Transparent bars
            statusBarColor = 0x00000000
            navigationBarColor = 0x00000000
        }
    }
    // Allow content to draw under system bars
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

/**
 * Controls the status bar icon color (light or dark).
 *
 * On Android R (API 30+) uses the modern [WindowInsetsController.setSystemBarsAppearance]
 * to set [WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS].
 *
 * On Android M–Q (API 23–29) uses the deprecated [View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR]
 * flag due to lack of better alternatives.
 *
 * @param isLight If `true`, status bar icons will be light (white). If `false`, they will be dark.
 */
fun Activity.setLightStatusBars(isLight: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.decorView.windowInsetsController?.let { controller ->
            if (isLight) {
                controller.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            } else {
                controller.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // For API 23–29
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = if (isLight) {
            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}