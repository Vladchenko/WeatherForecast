package io.github.vladchenko.weatherforecast.presentation.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.domain.model.Coordinate
import io.github.vladchenko.weatherforecast.core.navigation.NavigationEventBus
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view.CitySearchScreen
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view.CurrentWeatherScreen
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.CITY_PARAM
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.CITY_SEARCH
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.LATITUDE_PARAM
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.LONGITUDE_PARAM
import io.github.vladchenko.weatherforecast.presentation.navigation.Route.WEATHER
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel

/**
 * Navigation host for the Weather Forecast app.
 *
 * Defines the navigation graph with two screens:
 * - [WEATHER] - Current weather screen with city details
 * - [CITY_SEARCH] - City search screen
 *
 * Navigation routes:
 * - Weather: weather/{city}/{lat}/{lon}
 * - City Search: city_search
 *
 * @param navController Navigation controller for screen routing
 * @param modifier Compose modifier
 * @param navigationEventBus The event bus for dispatching navigation events
 * @param appBarViewModel Shared view model for app bar state
 * @param hourlyViewModel Shared view model for hourly forecast
 * @param citySearchViewModel Shared view model for city search
 * @param weatherViewModel Shared view model for current weather
 */
@ExperimentalMaterial3Api
@Composable
fun WeatherAppNavHost(
    navController: NavController,
    modifier: Modifier = Modifier,
    navigationEventBus: NavigationEventBus,
    appBarViewModel: AppBarViewModel = hiltViewModel(),
    hourlyViewModel: HourlyWeatherViewModel = hiltViewModel(),
    citySearchViewModel: CitySearchViewModel = hiltViewModel(),
    weatherViewModel: CurrentWeatherViewModel = hiltViewModel(),
) {
    NavHost(
        navController = navController as NavHostController,
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
                cityModel = CityLocationModel (city, Coordinate(lat, lon)),
                appBarViewModel = appBarViewModel,
                hourlyViewModel = hourlyViewModel,
                weatherViewModel = weatherViewModel,
                navigationEventBus = navigationEventBus
            )
        }

        composable(route = CITY_SEARCH) {
            CitySearchScreen(
                appBarViewModel = appBarViewModel,
                navigationEventBus = navigationEventBus,
                citySearchViewModel = citySearchViewModel
            )
        }
    }
}
