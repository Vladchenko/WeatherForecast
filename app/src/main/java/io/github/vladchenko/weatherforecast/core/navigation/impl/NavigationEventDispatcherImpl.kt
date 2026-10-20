package io.github.vladchenko.weatherforecast.core.navigation.impl

import android.app.Activity
import androidx.navigation.NavController
import androidx.navigation.navOptions
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.navigation.api.NavigationEvent
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.AI_WEATHER_SUMMARY
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.CITY_SEARCH
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.SETTINGS
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.WEATHER

/**
 * Default implementation of [NavigationEventDispatcher] that handles navigation events
 * by directly interacting with [NavController].
 *
 * This class handles five types of navigation events:
 * - [NavigationEvent.ShowWeatherFor]: Navigate to the weather screen for the
 *   specified city. The route is formed from a pre-formatted and
 *   URL-encoded city name along with latitude and longitude. Fade animation is applied.
 * - [NavigationEvent.NavigateUp]: Pops the current destination from the navigation stack.
 * - [NavigationEvent.CloseApp]: Finishes the host activity via
 *   `Activity.finishAffinity()`, obtained from [NavController.context].
 * - [NavigationEvent.NavigateToCitySelection]: Navigate to the [CITY_SEARCH] screen
 *   using `navOptions` passed in the event, or default
 *   [NavAnimationUtils.fadeNavOptions] if not provided.
 * - [NavigationEvent.NavigateToSettings]: Navigate to the [SETTINGS] screen.
 * - [NavigationEvent.NavigateToAiSummary]: Navigate to the AI weather summary screen.
 *
 * Navigation operations are executed synchronously when [navigate] is called,
 * making this dispatcher a simple imperative implementation.
 *
 * @property navController [NavController] used to perform navigation operations.
 */
class NavigationEventDispatcherImpl(
    private val navController: NavController,
) : NavigationEventDispatcher {

    override fun navigate(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.ShowWeatherFor -> {
                val navOptions = navOptions {
                    anim {
                        enter = R.anim.fade_in
                        exit = R.anim.fade_out
                        popEnter = R.anim.fade_in
                        popExit = R.anim.fade_out
                    }
                }
                navController.navigate(
                    route = "$WEATHER/${event.city}/${event.latitude}/${event.longitude}",
                    navOptions = navOptions
                )
            }

            is NavigationEvent.NavigateUp -> {
                navController.popBackStack()
            }

            is NavigationEvent.CloseApp -> {
                (navController.context as? Activity)?.finishAffinity()
            }

            is NavigationEvent.NavigateToCitySelection -> {
                val navOptions = event.navOptions ?: NavAnimationUtils.fadeNavOptions()
                navController.navigate(CITY_SEARCH, navOptions)
            }

            is NavigationEvent.NavigateToSettings -> {
                navController.navigate(SETTINGS)
            }

            is NavigationEvent.NavigateToAiSummary -> {
                navController.navigate(AI_WEATHER_SUMMARY)
            }
        }
    }
}