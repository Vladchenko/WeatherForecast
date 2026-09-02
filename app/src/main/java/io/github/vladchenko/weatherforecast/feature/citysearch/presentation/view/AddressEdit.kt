package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.vladchenko.weatherforecast.core.domain.model.CityModel
import io.github.vladchenko.weatherforecast.core.navigation.NavigationEventBus
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.event.CitySelectionEvent
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.model.RecentCities
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEvent
import kotlinx.collections.immutable.ImmutableList

/**
 * A composable wrapper for the city search input field with integrated auto-complete UI.
 *
 * This component connects user actions from [AutoCompleteUI] to [CitySelectionEvent] events.
 * It automatically triggers loading of recent cities when the input field receives focus
 * for the first time.
 *
 * @param cityMask The current city mask to search for
 * @param queryLabel The label text displayed in the search field
 * @param modifier The modifier to be applied to the container
 * @param mainContentColor The primary UI color used for styling
 * @param onRecentsDelete Callback invoked when the user requests deletion of all recent cities
 * @param navigationEventBus The event bus for dispatching navigation events
 * @param recentCities The current recent cities data, wrapped in [WeatherUiState]
 * @param onCitySelectionEvent Callback for dispatching city selection events
 * @param cityMaskPredictions The current list of prediction results, wrapped in [WeatherUiState]
 */
@Composable
fun AddressEdit(
    cityMask: String,
    queryLabel: String,
    modifier: Modifier,
    mainContentColor: Color,
    onRecentsDelete: () -> Unit,
    navigationEventBus: NavigationEventBus,
    recentCities: WeatherUiState<RecentCities>?,
    onCitySelectionEvent: (CitySelectionEvent) -> Unit,
    cityMaskPredictions: WeatherUiState<ImmutableList<CityModel>>?
) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        AutoCompleteUI(
            query = cityMask,
            useOutlined = true,
            queryLabel = queryLabel,
            modifier = Modifier.fillMaxWidth(),
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
                navigationEventBus.send(
                    event =
                        NavigationEvent.ShowWeatherFor(
                            CityModel(
                                name = selectedCity.name,
                                state = selectedCity.state,
                                country = selectedCity.country,
                                latitude = selectedCity.latitude,
                                longitude = selectedCity.longitude
                            )
                        )
                )
                onCitySelectionEvent(CitySelectionEvent.SaveCityToRecents(selectedCity))
                onCitySelectionEvent(CitySelectionEvent.ClearQuery)
            },
            onRecentsDelete = onRecentsDelete
        )
    }
}