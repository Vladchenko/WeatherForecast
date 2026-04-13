package com.example.weatherforecast.presentation.view.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.presentation.viewmodel.forecast.DataSource
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
                Column {
                    predictions.data.forEach { city ->
                        CitySuggestionItem(
                            city = city,
                            mainContentColor = mainContentColor,
                            onItemClick = onItemClick
                        )
                    }
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

@Preview(showBackground = true, name = "CityPredictionsSection - Success")
@Composable
private fun CityPredictionsSectionSuccessPreview() {
    val predictions = listOf(
        CityDomainModel(name = "London", state = "England", country = "GB", lat = 51.5074, lon = -0.1278),
        CityDomainModel(name = "Los Angeles", state = "CA", country = "US", lat = 34.0522, lon = -118.2437)
    )
    CityPredictionsSection(
        predictions = WeatherUiState.Success(data = predictions, DataSource.LOCAL),
        mainContentColor = Color.White,
        onItemClick = {}
    )
}

@Preview(showBackground = true, name = "CityPredictionsSection - Empty")
@Composable
private fun CityPredictionsSectionEmptyPreview() {
    CityPredictionsSection(
        predictions = WeatherUiState.Success(data = emptyList(), DataSource.LOCAL),
        mainContentColor = Color.White,
        onItemClick = {}
    )
}