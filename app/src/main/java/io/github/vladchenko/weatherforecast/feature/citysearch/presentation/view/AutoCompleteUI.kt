package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.ui.state.DataSource
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.model.RecentCities

/**
 * Composable function that displays a search interface with auto-complete functionality.
 *
 * Shows a text field for user input and dynamically displays either:
 * - A list of city predictions based on the current query (when input is not empty)
 * - A list of recently searched cities (when input is empty)
 *
 * Features:
 * - Debounced search triggering via [onQueryChanged]
 * - Visual clear button when text is present
 * - Keyboard "Done" action support
 * - Focus handling to load recent cities on first interaction
 * - Delete all recent cities option
 *
 * @param modifier Modifier for outer layout
 * @param query Current search query string
 * @param queryLabel Label shown in the search field
 * @param useOutlined Whether to use outlined text field style
 * @param mainContentColor Primary text/icon color
 * @param onQueryChanged Called when user types in the search field
 * @param predictions Current state of city prediction results
 * @param recentCities Current state of recently used cities
 * @param onDoneActionClick Called when keyboard "Done" is pressed
 * @param onClearClick Called when clear button is clicked
 * @param onItemClick Called when a city item is selected
 * @param onFirstFocus Called the first time the field gains focus (used to load recents)
 * @param onRecentsDelete Called when user taps delete icon next to "Recent cities"
 */
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
    onFirstFocus: () -> Unit,
    onRecentsDelete: () -> Unit
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent cities:",
                        modifier = Modifier.padding(start = 16.dp),
                        color = mainContentColor.copy(alpha = 0.6f),
                    )
                    Image(
                        modifier = Modifier.clickable(
                            onClick = onRecentsDelete
                        ),
                        colorFilter = ColorFilter.tint(color = mainContentColor),
                        painter = painterResource(id = R.drawable.baseline_delete_forever_24),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                    )
                }
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
        } else {
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
private fun AutoCompleteUINoRecentPreview() {
    val recentCities = RecentCities(
        cities = emptyList()
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
        onFirstFocus = {},
        onRecentsDelete = {}
    )
}

@Preview(showBackground = true, name = "AutoCompleteUI - Recent Cities")
@Composable
private fun AutoCompleteUIRecentPreview() {
    val recentCities = RecentCities(
        cities = listOf(
            CityDomainModel(
                name = "Berlin",
                state = "BE",
                country = "DE",
                lat = 52.5200,
                lon = 13.4050
            ),
            CityDomainModel(
                name = "Madrid",
                state = "MD",
                country = "ES",
                lat = 40.4168,
                lon = -3.7038
            )
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
        onFirstFocus = {},
        onRecentsDelete = {}
    )
}

@Preview(showBackground = true, name = "AutoCompleteUI - Search Results")
@Composable
private fun AutoCompleteUISearchPreview() {
    val predictions = WeatherUiState.Success(
        listOf(
            CityDomainModel(
                name = "Chicago",
                state = "IL",
                country = "US",
                lat = 41.8781,
                lon = -87.6298
            ),
            CityDomainModel(
                name = "Chisinau",
                state = "MC",
                country = "MD",
                lat = 47.0167,
                lon = 28.8489
            )
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
        onFirstFocus = {},
        onRecentsDelete = {}
    )
}