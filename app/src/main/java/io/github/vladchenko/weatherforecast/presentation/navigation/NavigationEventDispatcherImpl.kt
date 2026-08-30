package io.github.vladchenko.weatherforecast.presentation.navigation

import androidx.navigation.NavController
import io.github.vladchenko.weatherforecast.core.navigation.NavigationEventBus
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.formatFullCityName
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.urlEncode
import io.github.vladchenko.weatherforecast.presentation.navigation.NavAnimationUtils.fadeNavOptions
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.CITY_SEARCH
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.weather

/**
 * Implementation of [NavigationEventDispatcher] that handles navigation events
 * by directly interacting with the [NavController].
 *
 * This class processes four types of navigation events:
 * - [NavigationEvent.ShowWeatherFor]: Navigates to the weather screen for a specific city
 * - [NavigationEvent.NavigateUp]: Pops the current destination from the back stack
 * - [NavigationEvent.CloseApp]: Dispatches an event to close the app
 * - [NavigationEvent.NavigateToCitySelection]: Navigates to city search screen,
 *   clearing the back stack up to and including the city search destination
 *
 * The navigation operations are executed synchronously when [navigate] is called,
 * making this a straightforward imperative navigation dispatcher.
 *
 * @property navController The [NavController] used to perform navigation operations
 * @property navigationEventBus The event bus for dispatching navigation events
 */
class NavigationEventDispatcherImpl(
    private val navController: NavController,
    private val navigationEventBus: NavigationEventBus
) : NavigationEventDispatcher {
    override fun navigate(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.ShowWeatherFor -> {
                navController.navigate(
                    route = weather(
                        city = formatFullCityName(
                            event.cityModel.name,
                            event.cityModel.state,
                            event.cityModel.country).urlEncode(),
                        lat = event.cityModel.latitude,
                        lon = event.cityModel.longitude
                    )
                )
            }

            is NavigationEvent.NavigateUp -> {
                navController.popBackStack()
            }

            is NavigationEvent.CloseApp -> {
                navigationEventBus.send(NavigationEvent.CloseApp)
            }

            is NavigationEvent.NavigateToCitySelection -> {
                val navOptions = event.navOptions ?: fadeNavOptions()
                navController.navigate(CITY_SEARCH, navOptions)
            }
        }
    }
}

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