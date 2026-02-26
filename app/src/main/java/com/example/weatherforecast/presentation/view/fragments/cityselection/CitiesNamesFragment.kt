package com.example.weatherforecast.presentation.view.fragments.cityselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import com.example.weatherforecast.presentation.viewmodel.cityselection.CityNavigationEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    @FlowPreview
    private val citiesNamesViewModel by activityViewModels<CitiesNamesViewModel>()

    @ExperimentalMaterial3Api
    @FlowPreview
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
                    onEvent = { event -> citiesNamesViewModel.onEvent(event) },
                    appBarViewModel = appBarViewModel,
                    viewModel = citiesNamesViewModel
                )
            }
        }
    }

    @FlowPreview
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                citiesNamesViewModel.navigationEvent.collectLatest { event ->
                    when (event) {
                        is CityNavigationEvent.NavigateUp -> findNavController().popBackStack()
                        is CityNavigationEvent.OpenWeatherFor -> openCurrentWeatherFragment(event.city)
                        null -> Unit
                    }
                }
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
    private fun openCurrentWeatherFragment(chosenCity: String) {
        val action =
            CitiesNamesFragmentDirections.actionCitiesNamesFragmentToCurrentTimeForecastFragment(
                chosenCity
            )
        findNavController().navigate(action)
    }
}