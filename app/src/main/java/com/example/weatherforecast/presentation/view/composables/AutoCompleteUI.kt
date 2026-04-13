package com.example.weatherforecast.presentation.view.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.models.domain.RecentCities
import com.example.weatherforecast.presentation.viewmodel.forecast.DataSource
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState

@Composable
fun AutoCompleteUI(
    modifier: Modifier,
    query: String,
    queryLabel: String,
    useOutlined: Boolean = false,
    mainContentColor: Color,
    onQueryChanged: (String) -> Unit,
    predictions: WeatherUiState<List<CityDomainModel>>?,
    recentCities: WeatherUiState<RecentCities>?,
    onDoneActionClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onItemClick: (CityDomainModel) -> Unit,
    onFirstFocus: () -> Unit
) {
    val view = LocalView.current
    var isFirstFocus by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        QuerySearch(
            query = query,
            label = queryLabel,
            useOutlined = useOutlined,
            mainContentColor = mainContentColor,
            onQueryChanged = onQueryChanged,
            onDoneActionClick = {
                keyboardController?.hide()
                view.clearFocus()
                onDoneActionClick()
            },
            onClearClick = onClearClick,
            onFocusChanged = { hasFocus ->
                if (hasFocus && isFirstFocus) {
                    isFirstFocus = false
                    onFirstFocus()
                }
            }
        )

        if (query.isBlank()) {
            if (recentCities is WeatherUiState.Success && recentCities.data.cities.isNotEmpty()) {
                Text(
                    text = "Recent",
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                    color = mainContentColor.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            LazyColumn(
                state = rememberLazyListState(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = TextFieldDefaults.MinHeight * 6),
                contentPadding = PaddingValues(top = 0.dp, bottom = 8.dp)
            ) {
                item {
                    CityListSection(
                        citiesState = recentCities,
                        mainContentColor = mainContentColor,
                        onItemClick = onItemClick,
                        emptyText = "No recent cities"
                    )
                }
            }
        }
        else {
            LazyColumn(
                state = rememberLazyListState(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = TextFieldDefaults.MinHeight * 6),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
            ) {
                item {
                    CityPredictionsSection(
                        predictions = predictions,
                        mainContentColor = mainContentColor,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "AutoCompleteUI - Recent Cities")
@Composable
private fun AutoCompleteUIRecentPreview() {
    val recentCities = RecentCities(
        cities = listOf(
            CityDomainModel(name = "Berlin", state = "BE", country = "DE", lat = 52.5200, lon = 13.4050),
            CityDomainModel(name = "Madrid", state = "MD", country = "ES", lat = 40.4168, lon = -3.7038)
        )
    )
    AutoCompleteUI(
        modifier = Modifier.fillMaxWidth(),
        query = "",
        queryLabel = "Enter city",
        useOutlined = true,
        mainContentColor = Color.White,
        onQueryChanged = {},
        predictions = null,
        recentCities = WeatherUiState.Success(data = recentCities, source = DataSource.LOCAL),
        onDoneActionClick = {},
        onClearClick = {},
        onItemClick = {},
        onFirstFocus = {}
    )
}

@Preview(showBackground = true, name = "AutoCompleteUI - Search Results")
@Composable
private fun AutoCompleteUISearchPreview() {
    val predictions = WeatherUiState.Success(
        listOf(
            CityDomainModel(name = "Chicago", state = "IL", country = "US", lat = 41.8781, lon = -87.6298),
            CityDomainModel( name = "Chisinau", state = "MC", country = "MD", lat = 47.0167, lon = 28.8489)
        ),
        source = DataSource.LOCAL
    )
    AutoCompleteUI(
        modifier = Modifier.fillMaxWidth(),
        query = "Chi",
        queryLabel = "Enter city",
        useOutlined = true,
        mainContentColor = Color.White,
        onQueryChanged = {},
        predictions = predictions,
        recentCities = null,
        onDoneActionClick = {},
        onClearClick = {},
        onItemClick = {},
        onFirstFocus = {}
    )
}