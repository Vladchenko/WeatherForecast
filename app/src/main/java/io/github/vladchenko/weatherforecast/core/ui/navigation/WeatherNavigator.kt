package io.github.vladchenko.weatherforecast.core.ui.navigation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.model.CurrentScreen
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.formatFullCityName
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view.CitySearchFragmentDirections
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view.WeatherFragmentDirections
import io.github.vladchenko.weatherforecast.presentation.viewmodel.cityselection.CityNavigationEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Handles navigation events emitted by [CitySearchViewModel].
 *
 * @property navController The navigation controller used for navigation actions
 *
 * Observes [CityNavigationEvent] and performs corresponding navigation actions
 * using the provided [NavController].
 */
class WeatherNavigator(private val navController: NavController) {

    /**
     * Starts observing navigation events from the given flow.
     *
     * Must be called within a lifecycle-aware scope (e.g., Fragment's onViewCreated).
     *
     * @param lifecycleOwner The lifecycle owner (usually Fragment)
     * @param navigationFlow Flow of navigation events to observe
     */
    fun start(
        lifecycleOwner: LifecycleOwner,
        navigationFlow: SharedFlow<CityNavigationEvent>
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                navigationFlow.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    /**
     * Returns the current navigation destination as a [CurrentScreen] enum value.
     *
     * Maps the current [NavController] destination to a high-level screen representation
     * used by the app's navigation logic. This allows other components (e.g., coordinators,
     * view models) to react to the current screen without direct access to [NavController].
     *
     * @return [CurrentScreen.Weather] if the current destination is the weather fragment,
     *         [CurrentScreen.CitySelection] if it's the city selection fragment,
     *         defaults to [CurrentScreen.Weather] for unknown destinations.
     */
    fun getCurrentDestination() = when (navController.currentDestination?.id) {
        R.id.currentTimeForecastFragment -> CurrentScreen.Weather
        R.id.citiesNamesFragment -> CurrentScreen.CitySelection
        else -> CurrentScreen.Weather
    }

    private fun handleEvent(event: CityNavigationEvent?) {
        when (event) {
            is CityNavigationEvent.CloseApp -> closeApp()
            is CityNavigationEvent.NavigateToCitySelection -> navigateToCitySelection()
            is CityNavigationEvent.NavigateUp -> navController.popBackStack()
            is CityNavigationEvent.OpenWeatherFor -> navigateToCurrentWeatherFragment(event.city)
            null -> Unit
        }
    }

    private fun navigateToCitySelection() {
        val action =
            WeatherFragmentDirections.actionCurrentTimeForecastFragmentToCitiesNamesFragment()
        navController.navigate(action, fadeNavOptions())
    }

    private fun navigateToCurrentWeatherFragment(city: CityDomainModel) {
        val action =
            CitySearchFragmentDirections.actionCitiesNamesFragmentToCurrentTimeForecastFragment(
                chosenCity = formatFullCityName(city.name, city.state, city.country),
                latitude = city.lat.toFloat(),
                longitude = city.lon.toFloat()
            )
        navController.navigate(action, fadeNavOptions())
    }

    private fun closeApp() {
        navController.context.getActivity()?.finishAffinity()
    }

    private fun fadeNavOptions(): NavOptions = navOptions {
        anim {
            enter = R.anim.fade_in
            exit = R.anim.fade_out
            popEnter = R.anim.fade_in
            popExit = R.anim.fade_out
        }
        launchSingleTop = true
        restoreState = true
    }

    private fun Context.getActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }
}