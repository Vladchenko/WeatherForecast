package com.example.weatherforecast.presentation.view.fragments.forecast.current

import android.location.Location
import android.location.LocationManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherforecast.R
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.presentation.CurrentWeatherUi
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.PresentationUtils.resolveColorAttr
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
    val context = LocalContext.current
    val forecastUiState = viewModel.forecastStateFlow.collectAsStateWithLifecycle()
    val appBarUiState = appBarViewModel.appBarStateFlow.collectAsStateWithLifecycle()
    val hourlyForecastUiState = hourlyViewModel.hourlyWeatherStateFlow.collectAsStateWithLifecycle()
    val fontSize by remember {
        derivedStateOf { PresentationUtils.getToolbarSubtitleFontSize(appBarUiState.value.subtitleSize) }
    }
    var showHourlyForecast by remember { mutableStateOf(false) }
    val isRefreshing by viewModel.isRefreshingStateFlow.collectAsState()
    val refreshState = rememberPullToRefreshState()

    // Разрешаем цвет атрибута в UI-слое, где есть правильный Context
    val statusColor = remember(appBarUiState.value) {
        context.resolveColorAttr(appBarUiState.value.subtitleColorAttr) // ← Теперь это R.attr.colorInfo, а не цвет!
    }

    LaunchedEffect(Unit) {
        viewModel.internetConnectedStateFlow
            .drop(1)    // Drop initial value as it's emitted immediately on collection start
            // and doesn't represent an actual connectivity change.
            .collect { isConnected ->
                val cityModel = viewModel.chosenCityStateFlow.value
                if (isConnected && cityModel != null) {
                    viewModel.launchWeatherForecast(
                        city = cityModel.city,
                        latitude = cityModel.location.latitude.toString(),
                        longitude = cityModel.location.longitude.toString()
                    )
                }
            }
    }

    LaunchedEffect(showHourlyForecast) {
        if (showHourlyForecast) {
            val city = (forecastUiState.value as? WeatherUiState.Success)?.data?.city.orEmpty()
            val coordinate =
                (forecastUiState.value as? WeatherUiState.Success)?.data?.coordinate
            val location = coordinate?.let { coordinate ->
                Location(LocationManager.NETWORK_PROVIDER).apply {
                    latitude = coordinate.latitude
                    longitude = coordinate.longitude
                }
            }
            if (location == null) return@LaunchedEffect 
            val cityModel = CityLocationModel(city, location)
            hourlyViewModel.getHourlyWeatherForLocation(cityModel)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = appBarUiState.value.title,
                            color = mainContentTextColor,
                        )
                        Text(
                            text = appBarUiState.value.subtitle,
                            color = statusColor,
                            fontSize = fontSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "backIcon",
                            tint = mainContentTextColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHourlyForecast = !showHourlyForecast }) {
                        Icon(Icons.Filled.Timeline, "hourlyForecast", tint = mainContentTextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundImage()
                PullToRefreshBox(
                    state = refreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        val cityModel = viewModel.chosenCityStateFlow.value
                        if (cityModel != null) {
                            viewModel.launchWeatherForecastFromPullToRefresh(
                                cityModel.city,
                                cityModel.location.latitude.toString(),
                                cityModel.location.longitude.toString()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter),
                            isRefreshing = isRefreshing,
                            state = refreshState
                        )
                    }
                ) {
                    when (val state = forecastUiState.value) {
                        is WeatherUiState.Loading -> {
                            AnimatedVisibility(
                                visible = viewModel.showProgressBarState.value,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                ShowProgressBar()
                            }
                        }

                        is WeatherUiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.message,
                                    fontSize = 32.sp,
                                    color = statusColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        is WeatherUiState.Success -> {
                            Column(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                AnimatedContent(
                                    targetState = state,
                                    transitionSpec = {
                                        // Сначала старый исчезает, потом новый появляется
                                        ContentTransform(
                                            targetContentEnter = fadeIn(
                                                animationSpec = tween(
                                                    durationMillis = 500,
                                                    delayMillis = 500
                                                )
                                            ),
                                            initialContentExit = fadeOut(
                                                animationSpec = tween(durationMillis = 500)
                                            )
                                        )
                                    },
                                    label = "WeatherContent"
                                ) { weatherState ->
                                    MainContent(
                                        innerPadding = PaddingValues(),
                                        mainContentTextColor = mainContentTextColor,
                                        onCityClick = onCityClick,
                                        uiState = weatherState
                                    )
                                }
                                AnimatedVisibility(visible = showHourlyForecast) {
                                    HourlyWeatherLayout(
                                        statusColor,
                                        hourlyWeather = hourlyForecastUiState.value,
                                    )
                                }
                            }
                        }
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
 */
@Composable
private fun BackgroundImage() {
    Image(
        painter = painterResource(id = R.drawable.background2),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .fillMaxSize(),
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
    uiState: WeatherUiState.Success<CurrentWeatherUi>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(top = 50.dp),
            text = uiState.data.dateTime,
            fontSize = 18.sp,
            color = mainContentTextColor
        )
        Text(
            modifier = Modifier
                .padding(start = 32.dp, top = 24.dp, end = 32.dp)
                .clickable {
                    onCityClick()
                },
            text = uiState.data.city,
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
                text = uiState.data.temperature,
                fontSize = 60.sp,
                color = mainContentTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = uiState.data.temperatureType,
                color = mainContentTextColor,
                fontSize = 30.sp,
            )
            Image(
                modifier = Modifier.padding(start = 8.dp),
                painter = painterResource(id = uiState.data.weatherIconId),
                contentDescription = uiState.data.weatherType
            )
        }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = uiState.data.weatherType,
            color = mainContentTextColor,
            fontSize = 18.sp
        )
    }
}

/**
 * Displays a full-screen semi-transparent overlay with a loading spinner.
 *
 * Uses a white background with 30% opacity to dim the underlying content.
 */
@Composable
fun ShowProgressBar() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .clip(shape = RoundedCornerShape(16.dp))
            .fillMaxSize()
            .alpha(0.3f)
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
