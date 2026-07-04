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
import androidx.compose.material3.MaterialTheme
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
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.rememberResolvedColorAttr
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.toToolbarSubtitleFontSize
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.event.CitySelectionEvent
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import kotlinx.coroutines.FlowPreview

/**
 * Full-screen layout for city selection with top app bar and background image.
 *
 * Composes:
 * - Top app bar with title and subtitle from [AppBarViewModel]
 * - Back navigation button triggering [CitySelectionEvent.NavigateUp]
 * - Background image and padding-safe content area
 * - Search input and city suggestions via AddressEdit
 *
 * Uses collectAsStateWithLifecycle to observe state from multiple view models.
 *
 * @param queryLabel Hint text for the search input field
 * @param citySelectionTitle Label shown above the search field
 * @param appBarViewModel Shared instance for toolbar state synchronization
 * @param citySearchViewModel ViewModel handling city search logic and state
 */
@Composable
@FlowPreview
@ExperimentalMaterial3Api
fun CitySelectionLayout(
    queryLabel: String,
    citySelectionTitle: String,
    appBarViewModel: AppBarViewModel,   // Передаём внешний ViewModel, так как инстанс нужен общий чтобы не терять связь с тулбаром
    citySearchViewModel: CitySearchViewModel
) {
    val appbarUiState by appBarViewModel.appBarUiStateFlow.collectAsStateWithLifecycle()
    val cityUiState by citySearchViewModel.cityMaskStateFlow.collectAsStateWithLifecycle()
    val cityPredictionsUiState by citySearchViewModel.cityPredictions.collectAsStateWithLifecycle()
    val recentCitiesNamesUiState by citySearchViewModel.recentCitiesNamesFlow.collectAsStateWithLifecycle()
    val fontSize = appbarUiState.subtitleSize.toToolbarSubtitleFontSize()

    val statusColor = rememberResolvedColorAttr(appbarUiState.subtitleColorAttr)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = appbarUiState.title,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = appbarUiState.subtitle,
                            color = statusColor,
                            fontSize = fontSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { citySearchViewModel.onEvent(CitySelectionEvent.NavigateUp) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "backIcon",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = citySelectionTitle,
                        modifier = Modifier.padding(top = 16.dp),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AddressEdit(
                        cityName = cityUiState,
                        queryLabel = queryLabel,
                        modifier = Modifier,
                        mainContentColor = MaterialTheme.colorScheme.onSurface,
                        cityMaskPredictions = cityPredictionsUiState,
                        recentCities = recentCitiesNamesUiState,
                        onEvent = citySearchViewModel::onEvent,
                        onRecentsDelete = citySearchViewModel::deleteRecents
                    )
                }
            }
        }
    )
}