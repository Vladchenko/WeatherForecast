package io.github.vladchenko.weatherforecast.feature.citysearch.presentation.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.toToolbarSubtitleFontSize
import io.github.vladchenko.weatherforecast.core.ui.utils.themeColor
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.event.CitySelectionEvent
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import kotlinx.coroutines.FlowPreview

/**
 * Full screen layout for city selection with top app bar and background.
 *
 * Composes the complete UI for choosing a city, including:
 * - Toolbar with title and subtitle from [AppBarViewModel]
 * - Navigation back button
 * - Background image
 * - Search input and suggestions via [AddressEdit]
 *
 * Observes multiple view model flows using [collectAsStateWithLifecycle].
 *
 * @param mainContentColor Color for text and icons
 * @param citySelectionTitle Title shown above the search field
 * @param queryLabel Hint text inside the search field
 * @param onEvent Dispatcher for user interaction events
 * @param appBarViewModel Provides toolbar state
 * @param viewModel Provides city search state and logic
 */
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun CitySelectionLayout(
    mainContentColor: Color = themeColor(R.attr.colorMainText),
    citySelectionTitle: String,
    queryLabel: String,
    onEvent: (CitySelectionEvent) -> Unit,
    appBarViewModel: AppBarViewModel,
    viewModel: CitySearchViewModel
) {
    val cityUiState by viewModel.cityMaskStateFlow.collectAsStateWithLifecycle()
    val appbarUiState by appBarViewModel.appBarStateFlow.collectAsStateWithLifecycle()
    val cityPredictionsUiState by viewModel.cityPredictions.collectAsStateWithLifecycle()
    val recentCitiesNamesUiState by viewModel.recentCitiesNamesFlow.collectAsStateWithLifecycle()
    val fontSize = appbarUiState.subtitleSize.toToolbarSubtitleFontSize()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = appbarUiState.title,
                            color = mainContentColor,
                        )
                        Text(
                            text = appbarUiState.subtitle,
                            color = mainContentColor,
                            fontSize = fontSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(CitySelectionEvent.NavigateUp) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon", tint = mainContentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        content = { innerPadding ->
            Image(
                painter = painterResource(id = R.drawable.background2),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = citySelectionTitle,
                        modifier = Modifier.padding(top = 16.dp),
                        fontSize = 16.sp,
                        color = mainContentColor
                    )
                    AddressEdit(
                        cityName = cityUiState,
                        queryLabel = queryLabel,
                        modifier = Modifier,
                        mainContentColor = mainContentColor,
                        cityMaskPredictions = cityPredictionsUiState,
                        recentCities = recentCitiesNamesUiState,
                        onEvent = onEvent,
                        onRecentsDelete = viewModel::deleteRecents
                    )
                }
            }
        }
    )
}