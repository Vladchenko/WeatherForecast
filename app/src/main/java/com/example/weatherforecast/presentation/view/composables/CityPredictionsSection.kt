package com.example.weatherforecast.presentation.view.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState

@Composable
fun CityPredictionsSection(
    predictions: WeatherUiState<List<CityDomainModel>>?,
    mainContentColor: Color,
    onItemClick: (CityDomainModel) -> Unit
) {
    when (predictions) {
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
        is WeatherUiState.Success -> {
            if (predictions.data.isNotEmpty()) {
                predictions.data.forEach { city ->
                    CitySuggestionItem(
                        city = city,
                        mainContentColor = mainContentColor,
                        onItemClick = onItemClick
                    )
                }
            } else {
                Text(
                    text = "No cities found",
                    modifier = Modifier.padding(16.dp),
                    color = mainContentColor.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
        null -> Unit
    }
}