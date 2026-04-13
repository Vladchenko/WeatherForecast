package com.example.weatherforecast.presentation.view.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.models.domain.RecentCities
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState

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