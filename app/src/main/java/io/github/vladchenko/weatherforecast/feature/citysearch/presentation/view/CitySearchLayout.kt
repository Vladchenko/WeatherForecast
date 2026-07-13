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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.rememberResolvedColorAttr
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.toToolbarSubtitleFontSize
import io.github.vladchenko.weatherforecast.feature.citysearch.domain.model.CityDomainModel
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.event.CitySelectionEvent
import io.github.vladchenko.weatherforecast.feature.recentcities.domain.model.RecentCities
import io.github.vladchenko.weatherforecast.models.presentation.AppBarUiState
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEvent
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEventDispatcher
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview

/**
 * Full-screen city selection layout with top app bar and background image.
 *
 * Renders:
 * - Top app bar (back button, title, subtitle) using [appBarUiState] from [AppBarViewModel]
 * - Background image and padded content area
 * - Search input and suggestions via [AddressEdit], including city predictions and recent cities
 *
 * Delegates all interactions (search, selection, navigation, history management) to [onCitySelectionEvent].
 *
 * @param queryLabel Hint text for the search input field
 * @param cityUiState Currently typed or selected city name (user input or saved city)
 * @param citySelectionTitle Label displayed above the search field
 * @param appBarUiState Toolbar state including title, subtitle, and styling
 * @param onCitySelectionEvent Callback for user actions: navigation, city selection, recent cities management
 * @param recentCitiesNamesUiState Recent cities data (optional)
 * @param cityPredictionsUiState City search suggestions (optional)
 */
@Composable
@FlowPreview
@ExperimentalMaterial3Api
fun CitySearchLayout(
    queryLabel: String,
    cityUiState: String,
    citySelectionTitle: String,
    appBarUiState: AppBarUiState,
    navigationDispatcher: NavigationEventDispatcher,
    onCitySelectionEvent: (CitySelectionEvent) -> Unit,
    recentCitiesNamesUiState: WeatherUiState<RecentCities>?,
    cityPredictionsUiState: WeatherUiState<ImmutableList<CityDomainModel>>?,
) {
    val statusColor = rememberResolvedColorAttr(appBarUiState.subtitleColorAttr)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = appBarUiState.title,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = appBarUiState.subtitle,
                            color = statusColor,
                            fontSize = appBarUiState.subtitleSize.toToolbarSubtitleFontSize(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigationDispatcher.navigate(NavigationEvent.NavigateUp) }) {
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
                        modifier = Modifier,
                        cityName = cityUiState,
                        queryLabel = queryLabel,
                        recentCities = recentCitiesNamesUiState,
                        navigationDispatcher = navigationDispatcher,
                        onCitySelectionEvent = onCitySelectionEvent,
                        cityMaskPredictions = cityPredictionsUiState,
                        mainContentColor = MaterialTheme.colorScheme.onSurface,
                        onRecentsDelete = { onCitySelectionEvent(CitySelectionEvent.ClearRecentCities) }
                    )
                }
            }
        }
    )
}