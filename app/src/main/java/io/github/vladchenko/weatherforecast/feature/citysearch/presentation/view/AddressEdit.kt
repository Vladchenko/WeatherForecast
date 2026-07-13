package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.event.CitySelectionEvent
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.model.RecentCities
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEvent
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEventDispatcher
import kotlinx.collections.immutable.ImmutableList

/**
 * A composable wrapper for the city search input field with integrated auto-complete UI.
 *
 * This component connects user actions from [AutoCompleteUI] to [CitySelectionEvent] events.
 * It automatically triggers loading of recent cities when the input field receives focus
 * for the first time.
 *
 * @param cityName The current city name or search query text
 * @param queryLabel The label text displayed in the search field
 * @param modifier The modifier to be applied to the container
 * @param mainContentColor The primary UI color used for styling
 * @param cityMaskPredictions The current list of prediction results, wrapped in [WeatherUiState]
 * @param recentCities The current recent cities data, wrapped in [WeatherUiState]
 * @param onRecentsDelete Callback invoked when the user requests deletion of all recent cities
 * @param navigationDispatcher Dispatcher for navigation events
 * @param onCitySelectionEvent Callback for dispatching city selection events
 */
@Composable
fun AddressEdit(
    cityName: String,
    queryLabel: String,
    modifier: Modifier,
    mainContentColor: Color,
    onRecentsDelete: () -> Unit,
    recentCities: WeatherUiState<RecentCities>?,
    navigationDispatcher: NavigationEventDispatcher,
    onCitySelectionEvent: (CitySelectionEvent) -> Unit,
    cityMaskPredictions: WeatherUiState<ImmutableList<CityDomainModel>>?
) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        AutoCompleteUI(
            modifier = Modifier.fillMaxWidth(),
            query = cityName,
            queryLabel = queryLabel,
            useOutlined = true,
            mainContentColor = mainContentColor,
            onQueryChanged = { updatedCityMask ->
                if (updatedCityMask.isNotBlank()) {
                    onCitySelectionEvent(CitySelectionEvent.UpdateQuery(updatedCityMask))
                }
            },
            predictions = cityMaskPredictions,
            recentCities = recentCities,
            onClearClick = { onCitySelectionEvent(CitySelectionEvent.ClearQuery) },
            onDoneActionClick = { /* handled inside */ },
            onFirstFocus = { onCitySelectionEvent(CitySelectionEvent.LoadRecentCities) },
            onItemClick = { selectedCity ->
                navigationDispatcher.navigate(
                    event =
                        NavigationEvent.ShowWeatherFor(
                            CityDomainModel(
                                name = selectedCity.name,
                                state = selectedCity.state,
                                country = selectedCity.country,
                                lat = selectedCity.lat,
                                lon = selectedCity.lon
                            )
                        )
                )
                onCitySelectionEvent(CitySelectionEvent.ClearQuery)
            },
            onRecentsDelete = onRecentsDelete
        )
    }
}