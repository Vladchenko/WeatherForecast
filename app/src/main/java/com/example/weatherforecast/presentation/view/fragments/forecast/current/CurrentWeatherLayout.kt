package com.example.weatherforecast.presentation.view.fragments.forecast.current

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.themeColor
import com.example.weatherforecast.presentation.view.fragments.forecast.hourly.HourlyWeatherLayout
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.CurrentWeatherViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.HourlyWeatherViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState
import kotlinx.coroutines.flow.drop

/**
 * Composable layout for the main weather forecast screen.
 *
 * Displays:
 * - A top app bar with title, subtitle, back button, and hourly forecast toggle
 * - Current weather data (city, temperature, weather condition, time)
 * - Optional hourly forecast panel (shown when toggled)
 * - Full-screen progress indicator during data loading
 *
 * The background is set using a static image that fills the screen.
 *
 * @param mainContentTextColor Color used for all main content text elements
 * @param onCityClick Callback invoked when the city name is clicked
 * @param onBackClick Callback invoked when the back button is pressed
 * @param appBarViewModel ViewModel managing the app bar state (title, subtitle, colors)
 * @param viewModel Main ViewModel providing current weather forecast state
 * @param hourlyViewModel ViewModel providing hourly forecast data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NonSkippableComposable
fun CurrentWeatherLayout(
    mainContentTextColor: Color,
    onCityClick: () -> Unit,
    onBackClick: () -> Unit,
    appBarViewModel: AppBarViewModel,
    viewModel: CurrentWeatherViewModel,
    hourlyViewModel: HourlyWeatherViewModel,
) {
    val forecastUiState = viewModel.forecastState.collectAsStateWithLifecycle()
    val appBarUiState = appBarViewModel.appBarState.collectAsStateWithLifecycle()
    val fontSize = remember {
        derivedStateOf { PresentationUtils.getToolbarSubtitleFontSize(appBarUiState.value.subtitle).sp }
    }
    var showHourlyForecast by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.internetConnectedState
            .drop(1)    // First entry is dropped, since redundant
            .collect { isConnected ->
            if (isConnected) {
                viewModel.launchWeatherForecast(viewModel.chosenCityFlow.value)
            }
        }
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
                            text = appBarUiState.value.title
                        )
                        Text(
                            modifier = Modifier,
                            text = appBarUiState.value.subtitle,
                            color = themeColor(appBarUiState.value.subtitleColorAttr),
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
                    IconButton(
                        onClick =
                            {
                                showHourlyForecast = !showHourlyForecast
                                hourlyViewModel.getHourlyForecastForCity(
                                    (forecastUiState.value as WeatherUiState.Success).forecast.city
                                )
                            }
                    ) {
                        Icon(Icons.Default.Timeline, "hourlyForecast")
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
                        mainContentTextColor,
                        onCityClick,
                        (forecastUiState.value as WeatherUiState.Success)
                    )
                    if (showHourlyForecast) {
                        HourlyWeatherLayout(
                            hourlyWeather = hourlyViewModel.hourlyForecastState.value,
                        )
                    }
                }
            }
        }
    )
}

/**
 * Displays the full-screen background image.
 *
 * The image covers the entire screen and respects scaffold padding.
 *
 * @param innerPadding Padding provided by [Scaffold] to account for system bars
 */
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

/**
 * Displays the main weather information: city, time, temperature, and weather condition.
 *
 * The city name is clickable and triggers [onCityClick].
 *
 * @param innerPadding Padding from the scaffold to prevent system UI overlap
 * @param mainContentTextColor Color applied to all text elements
 * @param onCityClick Callback triggered when the user taps the city name
 * @param uiState Success state containing the current weather data
 */
@Composable
private fun MainContent(
    innerPadding: PaddingValues,
    mainContentTextColor: Color,
    onCityClick: () -> Unit,
    uiState: WeatherUiState.Success,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = 50.dp),
            text = uiState.forecast.dateTime,
            fontSize = 18.sp,
            color = mainContentTextColor
        )
        Text(
            modifier = Modifier
                .padding(start = 32.dp, top = 24.dp, end = 32.dp)
                .clickable {
                    onCityClick()
                },
            text = uiState.forecast.city,
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
                text = uiState.forecast.temperature,
                fontSize = 60.sp,
                color = mainContentTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = uiState.forecast.temperatureType,
                color = mainContentTextColor,
                fontSize = 30.sp,
            )
            Image(
                modifier = Modifier.padding(start = 8.dp),
                painter = painterResource(id = uiState.forecast.weatherIconId),
                contentDescription = uiState.forecast.weatherType
            )
        }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = uiState.forecast.weatherType,
            color = mainContentTextColor,
            fontSize = 18.sp
        )
    }
}

/**
 * Displays a full-screen semi-transparent overlay with a loading spinner.
 *
 * Uses a white background with 60% opacity to dim the underlying content.
 */
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
