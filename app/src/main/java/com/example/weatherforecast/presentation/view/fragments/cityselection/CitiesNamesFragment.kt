package com.example.weatherforecast.presentation.view.fragments.cityselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for selecting a city from a list of suggestions based on user input.
 *
 * Displays a search interface with auto-complete functionality, allowing users to type
 * and select a city name. Upon selection, navigates to the weather forecast screen.
 *
 * Uses [CitySelectionLayout] as its Compose UI root and shares ViewModels with the host activity.
 */
@AndroidEntryPoint
class CitiesNamesFragment : Fragment() {

    private val appBarViewModel by activityViewModels<AppBarViewModel>()
    private val citiesNamesViewModel by activityViewModels<CitiesNamesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CitySelectionLayout(
                    toolbarTitle = getString(R.string.app_name),
                    citySelectionTitle = getString(R.string.city_selection_from_dropdown),
                    queryLabel = getString(R.string.city_typing_begin),
                    { findNavController().popBackStack() },
                    { openForecastFragment(it) },   // This call should be done in viewmodel,
                    // but in this very case, somehow, CitiesNamesViewModel's livedata that calls
                    // forecastscreen, fires right away when a CitiesNames screen is opened (
                    // i.e. CurrentForecast -> CitiesNames -> CurrentForecast call is performed)
                    appBarViewModel = appBarViewModel,
                    viewModel = citiesNamesViewModel
                )
            }
        }
    }

    /**
     * Navigates to the current weather forecast screen for the specified city.
     *
     * Called when a city is selected from the suggestions list.
     *
     * @param chosenCity Name of the city to display weather for
     */
    private fun openForecastFragment(chosenCity: String) {
        val action =
            CitiesNamesFragmentDirections.actionCitiesNamesFragmentToCurrentTimeForecastFragment(
                chosenCity
            )
        findNavController().navigate(action)
    }
}