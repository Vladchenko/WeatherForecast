package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vladchenko.weatherforecast.core.ui.component.ProgressBar
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.model.RecentCities

/**
 * Composable that displays a section of cities based on [WeatherUiState].
 *
 * Handles three states:
 * - Success: shows list of cities or an empty message
 * - Loading: shows a progress bar
 * - Error: shows an error message
 *
 * Used for displaying both recent cities and search predictions.
 *
 * @param citiesState Data state containing the list of cities
 * @param mainContentColor Text color for items
 * @param onItemClick Callback triggered when a city item is clicked
 * @param emptyText Optional text to show when the list is empty
 */
@Composable
fun CityListSection(
    citiesState: WeatherUiState<RecentCities>?,
    mainContentColor: Color,
    onItemClick: (CityDomainModel) -> Unit,
    emptyText: String = "No cities found"
) {
    when (citiesState) {
        is WeatherUiState.Success -> {
            if (citiesState.data.cities.isEmpty()) {
                Text(
                    text = emptyText,
                    modifier = Modifier.padding(start = 16.dp),
                    color = mainContentColor.copy(alpha = 0.6f),
                )
            } else {
                Column {
                    citiesState.data.cities.forEach { city ->
                        CitySuggestionItem(
                            city = city,
                            mainContentColor = mainContentColor,
                            onItemClick = onItemClick
                        )
                    }
                }
            }
        }
        is WeatherUiState.Loading -> {
            ProgressBar()
        }
        is WeatherUiState.Error -> {
            Text(
                text = "Error loading cities",
                modifier = Modifier.padding(16.dp),
                color = Color.Red,
                fontSize = 14.sp
            )
        }
        null -> Unit
    }
}