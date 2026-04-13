package com.example.weatherforecast.presentation.view.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.models.domain.RecentCities
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitySelectionEvent
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState

@Composable
fun AddressEdit(
    cityName: String,
    queryLabel: String,
    modifier: Modifier,
    mainContentColor: Color,
    cityMaskPredictions: WeatherUiState<List<CityDomainModel>>?,
    recentCities: WeatherUiState<RecentCities>?,
    onEvent: (CitySelectionEvent) -> Unit
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
                    onEvent(CitySelectionEvent.UpdateQuery(updatedCityMask))
                }
            },
            predictions = cityMaskPredictions,
            recentCities = recentCities,
            onClearClick = { onEvent(CitySelectionEvent.ClearQuery) },
            onDoneActionClick = { /* handled inside */ },
            onFirstFocus = { onEvent(CitySelectionEvent.LoadRecentCities) },
            onItemClick = { selectedCity ->
                onEvent(
                    CitySelectionEvent.SelectCity(
                        CityDomainModel(
                            name = selectedCity.name,
                            state = selectedCity.state,
                            country = selectedCity.country,
                            lat = selectedCity.lat,
                            lon = selectedCity.lon
                        )
                    )
                )
                onEvent(CitySelectionEvent.ClearQuery)
            }
        )
    }
}