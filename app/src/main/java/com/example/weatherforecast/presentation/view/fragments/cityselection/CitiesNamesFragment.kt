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
 * Represents a feature of choosing a city to have a weather forecast for it further on.
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
     * Open forecast fragment to show data on a [chosenCity] in it.
     */
    private fun openForecastFragment(chosenCity: String) {
        val action =
            CitiesNamesFragmentDirections.actionCitiesNamesFragmentToCurrentTimeForecastFragment(
                chosenCity
            )
        findNavController().navigate(action)
    }
}