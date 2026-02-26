package com.example.weatherforecast.presentation.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.example.weatherforecast.presentation.view.fragments.cityselection.CitiesNamesFragmentDirections
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
        navigationFlow: SharedFlow<CityNavigationEvent?>
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                navigationFlow.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    private fun handleEvent(event: CityNavigationEvent?) {
        when (event) {
            is CityNavigationEvent.NavigateUp -> navController.popBackStack()
            is CityNavigationEvent.OpenWeatherFor -> openCurrentWeatherFragment(event.city)
            null -> Unit
        }
    }

    private fun openCurrentWeatherFragment(city: String) {
        val action =
            CitiesNamesFragmentDirections.actionCitiesNamesFragmentToCurrentTimeForecastFragment(city)
        navController.navigate(action)
    }
}