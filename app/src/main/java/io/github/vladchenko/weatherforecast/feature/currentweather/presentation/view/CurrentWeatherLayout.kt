package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vladchenko.weatherforecast.core.domain.model.CityLocationModel
import io.github.vladchenko.weatherforecast.core.ui.state.WeatherUiState
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.getWeatherBackgroundResource
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.rememberResolvedColorAttr
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.toToolbarSubtitleFontSize
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.models.CurrentWeatherUi
import io.github.vladchenko.weatherforecast.feature.geolocation.util.createLocation
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.domain.model.HourlyWeather
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.view.HourlyWeatherLayout
import io.github.vladchenko.weatherforecast.models.presentation.AppBarUiState
import io.github.vladchenko.weatherforecast.presentation.navigation.NavAnimationUtils.fadeNavOptions
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEvent
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEventDispatcher

/**
 * Main weather screen layout.
 *
 * Displays current weather with optional hourly forecast panel, pull-to-refresh.
 *
 * ## Architecture
 * - Accepts **immutable** `appBarUiState`, `weatherUiState`, `refreshingState` — recomposes only when *new instances* are passed
 * - **No ViewModel or StateFlow dependency** — fully decoupled from business logic
 * - Delegates events to [CurrentWeatherViewModel] via [onRefreshWeather] and [onLoadHourlyWeather]
 * - Uses `AnimatedContent` with staggered enter/exit for smooth state transitions
 *
 * ## State behavior
 * - All UI states (`appBarUiState`, `weatherUiState`, `refreshingState`, `hourlyWeatherUiState`) are collected by parent (`WeatherFragment`)
 * - `CurrentWeatherLayout` receives plain immutable objects, making it:
 *   - Fully testable without ViewModel mocking
 *   - Lifecycle-agnostic (works in `Fragment`, `Dialog`, `ModalBottomSheet`)
 *   - Optimised for recomposition (Compose skips re-render if reference unchanged)
 *
 * ## Key events
 * - Toggling hourly forecast triggers [onLoadHourlyWeather] with resolved city location (via [CityLocationModel])
 * - Pull-to-refresh fires [CurrentWeatherEvent.RefreshWeather]
 * - City click fires [CurrentWeatherEvent.NavigateToCitySelection]
 * - Back button fires [CurrentWeatherEvent.NavigateUp]
 *
 * @param refreshingState The current weather refresh state (controls pull-to-refresh indicator).
 * @param appBarUiState The app bar UI state (title, subtitle, colors, visibility).
 * @param onRefreshWeather Handler for refresh events.
 * @param weatherUiState The current weather UI state (success/loading/error with data).
 * @param onLoadHourlyWeather Callback invoked when the hourly forecast is toggled on;
 *                            receives the resolved city location.
 * @param hourlyWeatherUiState The hourly forecast UI state (can be null during initial load).
 * @param navigationEventDispatcher Dispatcher for handling navigation events.
 */
@ExperimentalMaterial3Api
@Composable
@NonSkippableComposable
fun CurrentWeatherLayout(
    refreshingState: Boolean,
    appBarUiState: AppBarUiState,
    onRefreshWeather: () -> Unit,
    weatherUiState: WeatherUiState<CurrentWeatherUi>,
    onLoadHourlyWeather: (CityLocationModel) -> Unit,
    hourlyWeatherUiState: WeatherUiState<HourlyWeather>?,
    navigationEventDispatcher: NavigationEventDispatcher
) {
    val refreshState = rememberPullToRefreshState()
    var showHourlyForecast by remember { mutableStateOf(false) }

    // Разрешаем цвет аттрибута в UI-слое, где есть правильный Context
    val statusColor = rememberResolvedColorAttr(appBarUiState.subtitleColorAttr)

    LaunchedEffect(showHourlyForecast) {
        if (!showHourlyForecast) return@LaunchedEffect
        when (weatherUiState) {
            is WeatherUiState.Success -> {
                val city = weatherUiState.data.city
                val coordinate = weatherUiState.data.coordinate
                val location = createLocation(coordinate.latitude, coordinate.longitude)
                onLoadHourlyWeather(CityLocationModel(city, location))
            }

            else -> return@LaunchedEffect
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
                    IconButton(onClick = { navigationEventDispatcher.navigate(NavigationEvent.CloseApp) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "backIcon",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHourlyForecast = !showHourlyForecast }) {
                        Icon(
                            Icons.Filled.Timeline,
                            "hourlyForecast",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        content = { innerPadding ->
            val weatherType by remember(weatherUiState) {
                derivedStateOf {
                    when (weatherUiState) {
                        is WeatherUiState.Success -> weatherUiState.data.weatherType
                        else -> ""
                    }
                }
            }
            BackgroundImage()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                PullToRefreshBox(
                    state = refreshState,
                    isRefreshing = refreshingState,
                    onRefresh = {
                        onRefreshWeather()
                    },
                    modifier = Modifier.fillMaxSize(),
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter),
                            isRefreshing = refreshingState,
                            state = refreshState
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(
                                PaddingValues(
                                    start = innerPadding.calculateStartPadding(
                                        LayoutDirection.Ltr
                                    ),
                                    top = innerPadding.calculateTopPadding(),
                                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                                )
                            )
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        AnimatedContent(
                            targetState = weatherUiState,
                            transitionSpec = {
                                // Сначала старый исчезает, потом новый появляется
                                ContentTransform(
                                    targetContentEnter = fadeIn(
                                        animationSpec = tween(
                                            durationMillis = 800,
                                            delayMillis = 800
                                        )
                                    ),
                                    initialContentExit = fadeOut(
                                        animationSpec = tween(durationMillis = 800)
                                    )
                                )
                            },
                            label = "WeatherContent",
                            modifier = Modifier.weight(1f)
                        ) { state ->
                            when (state) {
                                is WeatherUiState.Loading -> {
                                    CurrentWeatherSkeleton(
                                        modifier = Modifier.fillMaxSize(),
                                        shimmerColors = ShimmerDefaults.colors(
                                            baseColor = Color.White.copy(alpha = 0.03f),
                                            highlightColor = Color.White.copy(alpha = 0.35f),
                                            durationMs = 2000,
                                        )
                                    )
                                }

                                is WeatherUiState.Error -> {
                                    Text(
                                        modifier = Modifier.padding(top = 80.dp),
                                        text = state.message,
                                        fontSize = 32.sp,
                                        color = statusColor,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                is WeatherUiState.Success -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(), // Только ширина, высоту контролирует AnimatedContent
                                        verticalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        MainContent(
                                            innerPadding = PaddingValues(),
                                            mainContentTextColor = MaterialTheme.colorScheme.onSurface,
                                            onCityClick = {
                                                navigationEventDispatcher.navigate(
                                                    NavigationEvent.NavigateToCitySelection(
                                                        fadeNavOptions()
                                                    )
                                                )
                                            },
                                            uiState = state
                                        )
                                        AnimatedVisibility(visible = showHourlyForecast) {
                                            HourlyWeatherLayout(
                                                itemWidth = 130.dp,
                                                itemHeight = 100.dp,
                                                statusColor,
                                                mainContentTextColor = MaterialTheme.colorScheme.onSurface,
                                                hourlyWeather = hourlyWeatherUiState,
                                            )
                                        }
                                    }
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
        contentScale = ContentScale.Crop,
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
            lineHeight = 40.sp,
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
