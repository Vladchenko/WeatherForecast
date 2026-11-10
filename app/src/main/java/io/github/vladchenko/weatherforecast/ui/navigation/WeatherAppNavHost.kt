package io.github.vladchenko.weatherforecast.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.CityModel
import io.github.vladchenko.weatherforecast.core.domain.model.Coordinate
import io.github.vladchenko.weatherforecast.core.navigation.api.NavigationEvent
import io.github.vladchenko.weatherforecast.core.navigation.api.NavigationEventBus
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.AI_WEATHER_SUMMARY
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.CITY_PARAM
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.CITY_SEARCH
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.LATITUDE_PARAM
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.LONGITUDE_PARAM
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.SETTINGS
import io.github.vladchenko.weatherforecast.core.navigation.api.Route.WEATHER
import io.github.vladchenko.weatherforecast.core.navigation.impl.NavAnimationUtils
import io.github.vladchenko.weatherforecast.core.navigation.impl.NavigationEventDispatcher
import io.github.vladchenko.weatherforecast.core.navigation.impl.NavigationEventDispatcherImpl
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.presentation.WeatherSummaryScreen
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view.CitySearchScreen
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view.CurrentWeatherScreen
import io.github.vladchenko.weatherforecast.feature.settings.SettingsScreen

/**
 * Navigation host for the Weather Forecast application.
 *
 * Defines the navigation graph with three screens:
 * - [WEATHER] — current weather screen with city details and path parameters
 * - [CITY_SEARCH] — city search screen for selecting a new location
 * - [SETTINGS] — user settings screen
 *
 * ## Navigation Routes
 * - Weather: `current_weather/{city}/{lat}/{lon}` — city name (URL-encoded),
 *   latitude ([NavType.FloatType]), longitude ([NavType.FloatType]).
 *   Default start destination: `current_weather/Moscow/55.7558/37.6173`.
 * - City Search: `city_search`
 * - Settings: `settings`
 *
 * ## Navigation Architecture
 * All navigation actions are sent through [NavigationEventBus], which generates
 * events collected by [NavigationEventHandler] (via `LaunchedEffect`) and passed
 * to [NavigationEventDispatcher] to perform actual [NavHostController] operations.
 * This ensures a single source of truth for navigation logic while keeping composables
 * independent of [NavHostController].
 *
 * ## City Selection Process
 * 1. User selects a city in [CitySearchScreen].
 * 2. [NavigationEvent.ShowWeatherFor] is sent through [NavigationEventBus].
 * 3. [NavigationEventHandler] receives the event and passes it to [NavigationEventDispatcher].
 * 4. [NavHostController] navigates to the weather route with new city parameters.
 * 5. [CurrentWeatherScreen] receives the new [CityLocationModel] and triggers weather fetching.
 *
 * @param modifier Compose modifier for NavHost.
 * @param navController Navigation controller for routing screens.
 * @param navigationEventBus Event bus for sending navigation events.
 * @param preferencesManager Preferences manager for storing user settings.
 */
@ExperimentalMaterial3Api
@Composable
fun WeatherAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    navigationEventBus: NavigationEventBus,
    preferencesManager: PreferencesManager,
) {
    val navigationDispatcher = remember(navController) {
        NavigationEventDispatcherImpl(navController)
    }

    NavigationEventHandler(
        navigationEventBus = navigationEventBus,
        navigationDispatcher = navigationDispatcher
    )

    NavHost(
        navController = navController,
        startDestination = "$WEATHER/Moscow/55.7558/37.6173",
        modifier = modifier
    ) {
        composable(
            route = "$WEATHER/{$CITY_PARAM}/{$LATITUDE_PARAM}/{$LONGITUDE_PARAM}",
            arguments = listOf(
                navArgument(CITY_PARAM) { type = NavType.StringType },
                navArgument(LATITUDE_PARAM) { type = NavType.FloatType },
                navArgument(LONGITUDE_PARAM) { type = NavType.FloatType },
            )
        ) { backStackEntry ->
            CurrentWeatherScreen(
                cityModel = CityLocationModel(
                    city = backStackEntry.arguments?.getString(CITY_PARAM)?.replace("%20", " ").orEmpty(),
                    coordinate = Coordinate(
                        latitude = backStackEntry.arguments?.getFloat(LATITUDE_PARAM)?.toDouble() ?: .0,
                        longitude = backStackEntry.arguments?.getFloat(LONGITUDE_PARAM)?.toDouble() ?: .0
                    )
                ),
                onCloseApp = { navigationEventBus.send(NavigationEvent.CloseApp) },
                onNavigateToSettings = {
                    navigationEventBus.send(NavigationEvent.NavigateToSettings)
                },
                onNavigateToCitySelection = {
                    navigationEventBus.send(
                        event = NavigationEvent.NavigateToCitySelection(
                            navOptions = NavAnimationUtils.fadeNavOptions()
                        )
                    )
                },
                onNavigateToAiSummary = {
                    navigationEventBus.send(
                        event = NavigationEvent.NavigateToAiSummary(
                            navOptions = NavAnimationUtils.fadeNavOptions()
                        )
                    )
                }
            )
        }

        composable(route = CITY_SEARCH) {
            CitySearchScreen(
                onNavigateUp = { navigationEventBus.send(NavigationEvent.NavigateUp) },
                onCitySelected = { selectedCity ->
                    navigationEventBus.send(
                        NavigationEvent.ShowWeatherFor(
                            city = formatFullCityNameForRoute(selectedCity),
                            latitude = selectedCity.latitude.toFloat(),
                            longitude = selectedCity.longitude.toFloat()
                        )
                    )
                }
            )
        }

        composable(route = SETTINGS) {
            SettingsScreen(
                onNavigateUp = { navigationEventBus.send(NavigationEvent.NavigateUp) },
                preferencesManager = preferencesManager
            )
        }

        composable(route = AI_WEATHER_SUMMARY) {
            WeatherSummaryScreen(
                onShare = {},   // TODO: Implement sharing
                onNavigateUp = { navigationEventBus.send(NavigationEvent.NavigateUp) },
            )
        }
    }
}

/**
 * Formats the full city name for the navigation route.
 *
 * Combines city name, state, and country into a URL-safe format.
 *
 * @param city Selected city model
 * @return Formatted city string for route parameter
 */
private fun formatFullCityNameForRoute(city: CityModel): String {
    val fullCity = buildString {
        append(city.name)
        if (!city.state.isNullOrBlank()) {
            append(", ${city.state}")
        }
        append(", ${city.country}")
    }
    return fullCity.replace(" ", "%20")
}

/**
 * Component for processing navigation events from the event bus.
 *
 * Collects the navigation event flow from [NavigationEventBus] and passes them
 * to [NavigationEventDispatcher] for performing navigation operations.
 *
 * @param navigationEventBus Event bus for receiving navigation events.
 * @param navigationDispatcher Dispatcher for performing navigation operations.
 */
@Composable
private fun NavigationEventHandler(
    navigationEventBus: NavigationEventBus,
    navigationDispatcher: NavigationEventDispatcher
) {
    LaunchedEffect(navigationEventBus, navigationDispatcher) {
        navigationEventBus.navigationEventFlow.collect { event ->
            navigationDispatcher.navigate(event)
        }
    }
}
