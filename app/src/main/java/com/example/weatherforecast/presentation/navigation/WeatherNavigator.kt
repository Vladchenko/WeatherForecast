package com.example.weatherforecast.presentation.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import com.example.weatherforecast.R
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.presentation.view.fragments.cityselection.CitiesNamesFragmentDirections
import com.example.weatherforecast.presentation.view.fragments.forecast.WeatherFragmentDirections
import com.example.weatherforecast.presentation.viewmodel.cityselection.CityNavigationEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Handles navigation events emitted by [CitiesNamesViewModel].
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

    fun navigateToCitySelection() {
        val action = WeatherFragmentDirections.actionCurrentTimeForecastFragmentToCitiesNamesFragment()
        navController.navigate(action, fadeNavOptions())
    }

    fun navigateUp() {
        navController.popBackStack()
    }

    private fun handleEvent(event: CityNavigationEvent?) {
        when (event) {
            is CityNavigationEvent.NavigateUp -> navController.popBackStack()
            is CityNavigationEvent.OpenWeatherFor -> openCurrentWeatherFragment(event.city)
            null -> Unit
        }
    }

    private fun openCurrentWeatherFragment(city: CityLocationModel) {
        val action =
            CitiesNamesFragmentDirections.actionCitiesNamesFragmentToCurrentTimeForecastFragment(
                city.city,
                city.location.latitude.toString(),
                city.location.longitude.toString())
        navController.navigate(action, fadeNavOptions())
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
}