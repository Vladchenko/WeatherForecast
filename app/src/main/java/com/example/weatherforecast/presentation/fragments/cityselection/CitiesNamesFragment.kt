package com.example.weatherforecast.presentation.fragments.cityselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Represents a feature of choosing a city to further have a weather forecast on.
 */
@AndroidEntryPoint
class CitiesNamesFragment : Fragment() {

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
                    queryLabel = getString(R.string.begin_city_typing),
                    { findNavController().popBackStack() },
                    { openForecastFragment(it) },   // This call should be done in viewmodel,
                    // but in this very case, somehow, CitiesNamesViewModel's livedata that calls
                    // forecastscreen, fires right away when a CitiesNames screen is opened (
                    // i.e. CurrentForecast -> CitiesNames -> CurrentForecast call is performed)
                    viewModel = citiesNamesViewModel
                )
            }
        }
    }

    /**
     * Open forecast fragment to show it on a [chosenCity]
     */
    private fun openForecastFragment(chosenCity: String) {
        val action =
            CitiesNamesFragmentDirections.actionCitiesNamesFragmentToCurrentTimeForecastFragment(
                chosenCity
            )
        findNavController().navigate(action)
    }
}