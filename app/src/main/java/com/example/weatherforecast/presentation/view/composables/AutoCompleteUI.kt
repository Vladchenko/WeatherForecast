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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.models.domain.RecentCities
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