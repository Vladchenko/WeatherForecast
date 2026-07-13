package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEventDispatcher
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import kotlinx.coroutines.FlowPreview

/**
 * The root composable for the city selection screen.
 *
 * This function manages navigation logic and composes the [CitySearchLayout]
 * using data from the provided view models.
 *
 * @param appBarViewModel The shared toolbar state provider. Default: Hilt-provided instance.
 * @param navigationEventDispatcher The dispatcher for handling navigation events.
 * @param citySearchViewModel The view model that handles search logic and state management.
 *                            Default: Hilt-provided instance.
 */
@ExperimentalMaterial3Api
@FlowPreview
@Composable
fun CitySearchScreen(
    appBarViewModel: AppBarViewModel = hiltViewModel(),
    navigationEventDispatcher: NavigationEventDispatcher,
    citySearchViewModel: CitySearchViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val appBarUiState by appBarViewModel.appBarUiStateFlow.collectAsStateWithLifecycle()
    val cityMaskUiState by citySearchViewModel.cityMaskStateFlow.collectAsStateWithLifecycle()
    val cityPredictionsUiState by citySearchViewModel.cityPredictions.collectAsStateWithLifecycle()
    val recentCitiesNamesUiState by citySearchViewModel.recentCitiesNamesFlow.collectAsStateWithLifecycle()
    CitySearchLayout(
        cityUiState = cityMaskUiState,
        appBarUiState = appBarUiState,
        cityPredictionsUiState = cityPredictionsUiState,
        navigationDispatcher = navigationEventDispatcher,
        recentCitiesNamesUiState = recentCitiesNamesUiState,
        queryLabel = context.getString(R.string.city_typing_begin),
        citySelectionTitle = context.getString(R.string.city_selection_hint),
        onCitySelectionEvent = { event -> citySearchViewModel.onCitySelectionEvent(event) },
    )
}