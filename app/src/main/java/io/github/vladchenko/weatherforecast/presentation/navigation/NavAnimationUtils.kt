package io.github.vladchenko.weatherforecast.presentation.navigation

import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import io.github.vladchenko.weatherforecast.R

/**
 * Utility object for creating common navigation animation options.
 */
object NavAnimationUtils {

    /**
     * Creates navigation options with fade animations.
     *
     * This is typically used for navigating to screens that should transition
     * smoothly without affecting the back stack behavior.
     */
    fun fadeNavOptions(): NavOptions = navOptions {
        anim {
            enter = R.anim.fade_in
            exit = R.anim.fade_out
            popEnter = R.anim.fade_in
            popExit = R.anim.fade_out
        }
        launchSingleTop = true
        restoreState = true
    }
}
