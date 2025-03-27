package com.example.weatherforecast.presentation.fragments.forecast

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.runtime.*
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
import com.example.weatherforecast.presentation.ui.components.HourlyForecastLayout
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel

/**
 * Layout for a main screen fragment
 */
@Composable
@NonSkippableComposable
fun CurrentTimeForecastLayout(
    toolbarTitle: String,
    currentDate: String,
    mainContentTextColor: Color,
    @DrawableRes weatherImageId: Int,
    onCityClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: WeatherForecastViewModel
) {
    val modifier = Modifier
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
                viewModel.launchWeatherForecast(viewModel.dataModelState.value?.city.orEmpty())
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            modifier = modifier.offset((-16).dp),
                            text = toolbarTitle
                        )
                        Text(
                            modifier = modifier
                                .offset((-16).dp)
                                .background(MaterialTheme.colors.primary.copy(alpha = 0.8f))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
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
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 10.dp
            )
        },
        content = { innerPadding ->
            BackgroundImage(modifier, innerPadding)
            AnimatedVisibility(
                visible = viewModel.showProgressBarState.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ShowProgressBar(modifier)
            }
            if (!viewModel.showProgressBarState.value) {
                Column(
                    modifier = modifier.fillMaxSize()
                ) {
                    MainContent(
                        modifier,
                        innerPadding,
                        currentDate,
                        mainContentTextColor,
                        onCityClick,
                        viewModel.dataModelState.value,
                        weatherImageId
                    )
                    if (showHourlyForecast) {
                        HourlyForecastLayout(
                            hourlyForecast = viewModel.hourlyForecastState.value,
                            modifier = modifier
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun BackgroundImage(
    modifier: Modifier,
    innerPadding: PaddingValues
) {
    Image(
        painter = painterResource(id = R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding),
    )
}

@Composable
private fun MainContent(
    modifier: Modifier,
    innerPadding: PaddingValues,
    currentDate: String,
    mainContentTextColor: Color,
    onCityClick: () -> Unit,
    dataModel: WeatherForecastDomainModel?,
    weatherImageId: Int
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = modifier.padding(top = 56.dp),
            text = currentDate,
            fontSize = 14.sp,
            color = mainContentTextColor
        )
        Text(
            modifier = modifier
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
            modifier = modifier
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
                modifier = modifier.padding(start = 8.dp),
                painter = painterResource(id = weatherImageId),
                contentDescription = dataModel?.weatherType
            )
        }
        Text(
            modifier = modifier.padding(top = 16.dp),
            text = dataModel?.weatherType.orEmpty(),
            color = mainContentTextColor
        )
    }
}

@Composable
fun ShowProgressBar(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(0.6f)
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
