package io.github.vladchenko.weatherforecast.presentation.navigation

import android.app.Activity
import androidx.navigation.NavController
import androidx.navigation.navOptions
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.formatFullCityName
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.urlEncode
import io.github.vladchenko.weatherforecast.presentation.navigation.NavAnimationUtils.fadeNavOptions
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.CITY_SEARCH
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.SETTINGS
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.weather

/**
 * Default implementation of [NavigationEventDispatcher] that handles navigation
 * events by directly interacting with the [NavController].
 *
 * This class processes five types of navigation events:
 * - [NavigationEvent.ShowWeatherFor]: Navigates to the weather screen for the
 *   specified city. The route is built from the full city name
 *   (via `formatFullCityName`) that is URL-encoded, together with latitude
 *   and longitude. A fade animation is applied.
 * - [NavigationEvent.NavigateUp]: Pops the current destination from the back stack.
 * - [NavigationEvent.CloseApp]: Finishes the hosting activity via
 *   `Activity.finishAffinity()`, resolved from [NavController.сontext].
 * - [NavigationEvent.NavigateToCitySelection]: Navigates to the [CITY_SEARCH]
 *   destination using the `navOptions` carried by the event, or the default
 *   `fadeNavOptions()` when none are provided.
 * - [NavigationEvent.NavigateToSettings]: Navigates to the [SETTINGS] destination.
 *
 * The navigation operations are executed synchronously when [navigate] is called,
 * making this a straightforward imperative navigation dispatcher.
 *
 * @property navController The [NavController] used to perform navigation operations.
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
                    route = weather(
                        city = formatFullCityName(
                            event.cityModel.name,
                            event.cityModel.state,
                            event.cityModel.country).urlEncode(),
                        lat = event.cityModel.latitude,
                        lon = event.cityModel.longitude
                    ),
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
                val navOptions = event.navOptions ?: fadeNavOptions()
                navController.navigate(CITY_SEARCH, navOptions)
            }

            is NavigationEvent.NavigateToSettings -> {
                navController.navigate(SETTINGS)
            }
        }
    }
}