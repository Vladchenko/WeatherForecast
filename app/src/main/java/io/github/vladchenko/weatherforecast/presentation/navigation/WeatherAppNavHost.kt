package io.github.vladchenko.weatherforecast.presentation.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.Coordinate
import io.github.vladchenko.weatherforecast.core.navigation.NavigationEventBus
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.event.CitySelectionEvent
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view.CitySearchScreen
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view.CurrentWeatherScreen
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.settings.SettingsScreen
import io.github.vladchenko.weatherforecast.presentation.navigation.NavAnimationUtils.fadeNavOptions
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.CITY_PARAM
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.CITY_SEARCH
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.LATITUDE_PARAM
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.LONGITUDE_PARAM
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.SETTINGS
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.WEATHER
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel

/**
 * Navigation host for the Weather Forecast app.
 *
 * Defines the navigation graph with three screens:
 * - [WEATHER] - Current weather screen with city details and path parameters
 * - [CITY_SEARCH] - City search screen for selecting a new location
 * - [SETTINGS] - User preferences screen
 *
 * ## Navigation Routes
 * - Weather: `current_weather/{city}/{lat}/{lon}` — city name (URL-encoded),
 *   latitude ([NavType.FloatType]), longitude ([NavType.FloatType]).
 *   Default start destination: `current_weather/Moscow/55.7558/37.6173`.
 * - City Search: `city_search`
 * - Settings: `settings`
 *
 * ## Navigation Architecture
 * All navigation actions are dispatched through [NavigationEventBus], which emits
 * events collected by [NavigationEventHandler] (via `LaunchedEffect`) and delegated
 * to [NavigationEventDispatcher] for actual [NavHostController] operations.
 * This provides a single source of truth for navigation logic, keeping composables
 * decoupled from the [NavHostController].
 *
 * ## City Selection Flow
 * 1. User selects a city in [CitySearchScreen].
 * 2. [NavigationEvent.ShowWeatherFor] is sent via [NavigationEventBus].
 * 3. [NavigationEventHandler] receives the event and forwards it to [NavigationEventDispatcher].
 * 4. [NavHostController] navigates to the weather route with the new city parameters.
 * 5. [CurrentWeatherScreen] receives the new [CityLocationModel] and triggers a weather fetch.
 *
 * @param modifier Compose modifier for the NavHost.
 * @param navController Navigation controller for screen routing.
 * @param navigationEventBus The event bus for dispatching navigation events.
 * @param preferencesManager The preferences manager for storing user preferences.
 * @param appBarViewModel Shared view model for app bar state.
 * @param hourlyViewModel Shared view model for hourly forecast.
 * @param citySearchViewModel Shared view model for city search.
 * @param weatherViewModel Shared view model for current weather.
 */
@ExperimentalMaterial3Api
@Composable
fun WeatherAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    navigationEventBus: NavigationEventBus,
    preferencesManager: PreferencesManager,
    appBarViewModel: AppBarViewModel = hiltViewModel(),
    hourlyViewModel: HourlyWeatherViewModel = hiltViewModel(),
    citySearchViewModel: CitySearchViewModel = hiltViewModel(),
    weatherViewModel: CurrentWeatherViewModel = hiltViewModel(),
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
            val city = backStackEntry.arguments?.getString(CITY_PARAM).orEmpty()
            val lat = backStackEntry.arguments?.getFloat(LATITUDE_PARAM)?.toDouble() ?: .0
            val lon = backStackEntry.arguments?.getFloat(LONGITUDE_PARAM)?.toDouble() ?: .0

            CurrentWeatherScreen(
                cityModel = CityLocationModel(city, Coordinate(lat, lon)),
                appBarViewModel = appBarViewModel,
                hourlyViewModel = hourlyViewModel,
                weatherViewModel = weatherViewModel,
                onCloseApp = { navigationEventBus.send(NavigationEvent.CloseApp) },
                onNavigateToSettings = {
                    navigationEventBus.send(NavigationEvent.NavigateToSettings)
                },
                onNavigateToCitySelection = {
                    navigationEventBus.send(
                        NavigationEvent.NavigateToCitySelection(
                            fadeNavOptions()
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
                        NavigationEvent.ShowWeatherFor(selectedCity)
                    )
                    citySearchViewModel.onCitySelectionEvent(
                        CitySelectionEvent.SaveCityToRecents(
                            selectedCity
                        )
                    )
                    citySearchViewModel.onCitySelectionEvent(CitySelectionEvent.ClearQuery)
                },
                appBarViewModel = appBarViewModel,
                citySearchViewModel = citySearchViewModel
            )
        }

        composable(route = SETTINGS) {
            SettingsScreen(
                onNavigateUp = { navigationEventBus.send(NavigationEvent.NavigateUp) },
                appBarViewModel = appBarViewModel,
                preferencesManager = preferencesManager
            )
        }
    }
}

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
