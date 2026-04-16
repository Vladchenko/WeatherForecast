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
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.model.RecentCities
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState

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
                    modifier = Modifier.padding(16.dp),
                    color = mainContentColor.copy(alpha = 0.6f),
                    fontSize = 14.sp
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