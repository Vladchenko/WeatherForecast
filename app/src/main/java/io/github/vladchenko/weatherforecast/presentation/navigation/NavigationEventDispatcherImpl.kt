package io.github.vladchenko.weatherforecast.presentation.navigation

import androidx.navigation.NavController
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.formatFullCityName
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.urlEncode
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.CITY_SEARCH
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.weather

/**
 * Implementation of [NavigationEventDispatcher] that handles navigation events
 * by directly interacting with the [NavController].
 *
 * This class processes four types of navigation events:
 * - [NavigationEvent.ShowWeatherFor]: Navigates to the weather screen for a specific city
 * - [NavigationEvent.NavigateUp]: Pops the current destination from the back stack
 * - [NavigationEvent.CloseApp]: Calls the [onCloseApp] callback to close the application
 * - [NavigationEvent.NavigateToCitySelection]: Navigates to city search screen,
 *   clearing the back stack up to and including the city search destination
 *
 * The navigation operations are executed synchronously when [navigate] is called,
 * making this a straightforward imperative navigation dispatcher.
 *
 * @param navController The [NavController] used to perform navigation operations
 * @param onCloseApp A callback that handles application closure logic
 */
class NavigationEventDispatcherImpl(
    val navController: NavController,
    val onCloseApp: () -> Unit
) : NavigationEventDispatcher {
    override fun navigate(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.ShowWeatherFor -> {
                navController.navigate(
                    route = weather(
                        city = formatFullCityName(
                            event.city.name,
                            event.city.state,
                            event.city.country).urlEncode(),
                        lat = event.city.lat,
                        lon = event.city.lon
                    )
                )
            }

            is NavigationEvent.NavigateUp -> {
                navController.popBackStack()
            }

            is NavigationEvent.CloseApp -> {
                onCloseApp()
            }

            is NavigationEvent.NavigateToCitySelection -> {
                navController.navigate(CITY_SEARCH) {
                    popUpTo(CITY_SEARCH) { inclusive = true } // или true, если нужно удалить весь стек
                    launchSingleTop = true
                }
            }
        }
    }
}