package io.github.vladchenko.weatherforecast.presentation.view.composables

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
import io.github.vladchenko.weatherforecast.presentation.PresentationUtils.toToolbarSubtitleFontSize
import io.github.vladchenko.weatherforecast.presentation.themeColor
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import io.github.vladchenko.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import io.github.vladchenko.weatherforecast.presentation.viewmodel.cityselection.CitySelectionEvent
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun CitySelectionLayout(
    mainContentColor: Color = themeColor(R.attr.colorMainText),
    citySelectionTitle: String,
    queryLabel: String,
    onEvent: (CitySelectionEvent) -> Unit,
    appBarViewModel: AppBarViewModel,
    viewModel: CitiesNamesViewModel
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
                        onEvent = onEvent
                    )
                }
            }
        }
    )
}