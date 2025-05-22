package com.example.weatherforecast.presentation.fragments.forecast.current

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.R
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.fragments.forecast.hourly.HourlyForecastLayout
import com.example.weatherforecast.presentation.viewmodel.forecast.HourlyForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel

/**
 * Layout for a main screen fragment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NonSkippableComposable
fun CurrentTimeForecastLayout(
    toolbarTitle: String,
    currentDate: String,
    mainContentTextColor: Color,
    @DrawableRes weatherImageId: Int,
    onCityClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: WeatherForecastViewModel,
    hourlyViewModel: HourlyForecastViewModel
) {
    val toolbarSubtitleState = viewModel.toolbarSubtitleMessageState.collectAsState()
    val toolbarSubtitle = toolbarSubtitleState.value.stringId?.let {
        stringResource(
            it,
            toolbarSubtitleState.value.valueForStringId.orEmpty()
        )
    } ?: toolbarSubtitleState.value.valueForStringId.orEmpty()
    val fontSize = remember {
        derivedStateOf { PresentationUtils.getToolbarSubtitleFontSize(toolbarSubtitle).sp }
    }
    var showHourlyForecast by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.internetConnectedState.collect { isConnected ->
            if (isConnected) {
                viewModel.launchWeatherForecast(viewModel.forecastState.value?.city.orEmpty())
            }
        }
    }

    LaunchedEffect(viewModel.chosenCityStateFlow) {
        viewModel.downloadRemoteForecastForCity(viewModel.chosenCityStateFlow.value)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = {
                    Column {
                        Text(
                            modifier = Modifier
                                .padding(top = 4.dp),
                            text = toolbarTitle
                        )
                        Text(
                            modifier = Modifier,
                            text = toolbarSubtitle,
                            color = toolbarSubtitleState.value.color,
                            fontSize = fontSize.value,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon")
                    }
                },
                actions = {
                    IconButton(onClick = { showHourlyForecast = !showHourlyForecast }) {
                        Icon(Icons.Default.Timeline, "hourlyForecast")
                        hourlyViewModel.loadHourlyForecastForCity(
                            viewModel.forecastState.value?.city.orEmpty()
                        )
                    }
                },
            )
        },
        content = { innerPadding ->
            BackgroundImage(innerPadding)
            AnimatedVisibility(
                visible = viewModel.showProgressBarState.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ShowProgressBar()
            }
            if (!viewModel.showProgressBarState.value) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    MainContent(
                        innerPadding,
                        currentDate,
                        mainContentTextColor,
                        onCityClick,
                        viewModel.forecastState.value,
                        weatherImageId
                    )
                    if (showHourlyForecast) {
                        HourlyForecastLayout(
                            hourlyForecast = hourlyViewModel.hourlyForecastState.value,
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun BackgroundImage(
    innerPadding: PaddingValues
) {
    Image(
        painter = painterResource(id = R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    )
}

@Composable
private fun MainContent(
    innerPadding: PaddingValues,
    currentDate: String,
    mainContentTextColor: Color,
    onCityClick: () -> Unit,
    dataModel: WeatherForecastDomainModel?,
    weatherImageId: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = 50.dp),
            text = currentDate,
            fontSize = 18.sp,
            color = mainContentTextColor
        )
        Text(
            modifier = Modifier
                .padding(start = 32.dp, top = 24.dp, end = 32.dp)
                .clickable {
                    onCityClick()
                },
            text = dataModel?.city.orEmpty(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = mainContentTextColor,
            fontSize = 36.sp,
        )
        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = dataModel?.temperature.orEmpty(),
                fontSize = 60.sp,
                color = mainContentTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = dataModel?.temperatureType.orEmpty(),
                color = mainContentTextColor,
                fontSize = 30.sp,
            )
            Image(
                modifier = Modifier.padding(start = 8.dp),
                painter = painterResource(id = weatherImageId),
                contentDescription = dataModel?.weatherType
            )
        }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = dataModel?.weatherType.orEmpty(),
            color = mainContentTextColor,
            fontSize = 18.sp
        )
    }
}

@Composable
fun ShowProgressBar() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.6f)
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
