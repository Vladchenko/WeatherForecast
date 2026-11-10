package io.github.vladchenko.weatherforecast.feature.aiweathersummary.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.MarkdownSegment
import io.github.vladchenko.weatherforecast.core.presentation.models.AppBarUiState
import io.github.vladchenko.weatherforecast.core.ui.component.BackgroundImage
import io.github.vladchenko.weatherforecast.core.ui.component.MarkdownText
import io.github.vladchenko.weatherforecast.core.ui.component.ShimmerDefaults
import io.github.vladchenko.weatherforecast.core.ui.component.SkeletonLoader
import io.github.vladchenko.weatherforecast.core.ui.constants.SubtitleSize
import io.github.vladchenko.weatherforecast.core.ui.status.TextType
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.rememberResolvedColorAttr
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.toToolbarSubtitleFontSize
import io.github.vladchenko.weatherforecast.ui.theme.WeatherForecastTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Compose layout for displaying the AI weather forecast summary.
 *
 * Displays the top bar with title, main content with transition animation
 * between states (loading, remote response, cached response, error),
 * as well as update and share buttons.
 *
 * @param onShare callback to share the summary.
 * @param onRefresh callback to refresh data.
 * @param onNavigateUp callback to navigate back.
 * @param appBarUiState top bar state.
 * @param weatherSummaryUiState AI summary state to display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherSummaryLayout(
    onShare: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateUp: () -> Unit,
    appBarUiState: AppBarUiState,
    weatherSummaryUiState: WeatherSummaryUiState
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
                            text = stringResource(appBarUiState.titleResId),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = when (appBarUiState.subtitle) {
                                is TextType.Text -> appBarUiState.subtitle.value
                                is TextType.ResId -> {
                                    stringResource(
                                        appBarUiState.subtitle.id,
                                        *appBarUiState.subtitle.args
                                    )
                                }

                                is TextType.Empty -> ""
                            },
                            color = statusColor,
                            fontSize = appBarUiState.subtitleSize.toToolbarSubtitleFontSize(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "backIcon",
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
            BackgroundImage()
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
            ) {
                AnimatedContent(
                    targetState = weatherSummaryUiState,
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
                        is WeatherSummaryUiState.Loading -> {
                            SkeletonLoader(
                                modifier = Modifier
                                    .padding(16.dp),
                                shimmerColors = ShimmerDefaults.colors(
                                    baseColor = Color.White.copy(alpha = 0.03f),
                                    highlightColor = Color.White.copy(alpha = 0.35f),
                                    durationMs = 2000,
                                )
                            )
                        }

                        is WeatherSummaryUiState.Remote -> {
                            Summary(
                                llm = state.llm,
                                onShare = onShare,
                                onRefresh = onRefresh,
                                summary = state.summary,
                                title = stringResource(R.string.ai_weather_summary_title)
                            )
                        }

                        is WeatherSummaryUiState.Local -> {
                            val dateFormatPattern = stringResource(R.string.date_format_pattern)
                            val formattedDate = remember(state.timeStamp) {
                                state.timeStamp?.let { timestamp ->
                                    val instant = Instant.ofEpochMilli(timestamp)
                                    val zoneId = ZoneId.systemDefault()
                                    val formatter = DateTimeFormatter.ofPattern(
                                        dateFormatPattern,
                                        Locale.getDefault()
                                    )
                                    formatter.format(instant.atZone(zoneId))
                                }
                            }
                            Summary(
                                llm = state.llm,
                                onShare = onShare,
                                onRefresh = onRefresh,
                                summary = state.summary,
                                title = stringResource(
                                    R.string.ai_weather_summary_local_title,
                                    formattedDate ?: ""
                                )
                            )
                        }

                        is WeatherSummaryUiState.Error -> {
                            Text(
                                modifier = Modifier.padding(
                                    top = 80.dp,
                                    start = 32.dp,
                                    end = 32.dp,
                                    bottom = 32.dp
                                ),
                                text = stringResource(id = state.messageId),
                                fontSize = 24.sp,
                                color = statusColor,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun Summary(
    llm: String,
    title: String,
    summary: List<MarkdownSegment>,
    onShare: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 16.dp
            )
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.15f
                    ),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 0.dp,
                    color = Color.Transparent
                ),
                onClick = onRefresh
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Text(stringResource(R.string.update))
            }
            OutlinedButton(
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.15f
                    ),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 0.dp,
                    color = Color.Transparent
                ),
                onClick = onShare
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Text(stringResource(R.string.share))
            }
            OutlinedButton(
                modifier = Modifier.clickable(enabled = false) {},
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.15f
                    ),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 0.dp,
                    color = Color.Transparent
                ),
                onClick = {}
            ) {
                Text(llm)
            }
        }
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            MarkdownText(
                segments = summary,
                lineHeight = 24.sp
            )
        }
    }
}

@Preview(
    name = "Loading State",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun WeatherSummaryLoadingPreview() {
    WeatherForecastTheme(darkTheme = true) {
        WeatherSummaryLayout(
            onShare = {},
            onRefresh = {},
            onNavigateUp = {},
            appBarUiState = AppBarUiState(
                titleResId = R.string.app_name,
                titleColorAttr = R.attr.colorInfo,
                subtitleSize = SubtitleSize.Small,
                subtitleColorAttr = R.attr.colorInfo,
                subtitle = TextType.Text("Loading forecast for New York")
            ),
            weatherSummaryUiState = WeatherSummaryUiState.Loading
        )
    }
}

@Preview(
    name = "Remote State",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun WeatherSummaryRemotePreview() {
    WeatherForecastTheme(darkTheme = true) {
        WeatherSummaryLayout(
            onShare = {},
            onRefresh = {},
            onNavigateUp = {},
            appBarUiState = AppBarUiState(
                titleResId = R.string.app_name,
                titleColorAttr = R.attr.colorInfo,
                subtitleSize = SubtitleSize.Small,
                subtitleColorAttr = R.attr.colorInfo,
                subtitle = TextType.Text("Forecast for New York loaded")
            ),
            weatherSummaryUiState = WeatherSummaryUiState.Remote(
                summary = listOf(
                    MarkdownSegment.Heading(1, "Current Weather"),
                    MarkdownSegment.Paragraph("Temperature: 22°C"),
                    MarkdownSegment.Paragraph("Conditions: Clear sky"),
                    MarkdownSegment.Heading(2, "Forecast"),
                    MarkdownSegment.Paragraph("The weather will remain clear for the next 24 hours.")
                ),
                llm = "Gemma 4 31B"
            )
        )
    }
}

@Preview(
    name = "Local State",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun WeatherSummaryLocalPreview() {
    WeatherForecastTheme(darkTheme = true) {
        WeatherSummaryLayout(
            onShare = {},
            onRefresh = {},
            onNavigateUp = {},
            appBarUiState = AppBarUiState(
                titleResId = R.string.app_name,
                titleColorAttr = R.attr.colorInfo,
                subtitleSize = SubtitleSize.Small,
                subtitleColorAttr = R.attr.colorInfo,
                subtitle = TextType.Text("Outdated forecast for New York")
            ),
            weatherSummaryUiState = WeatherSummaryUiState.Local(
                summary = listOf(
                    MarkdownSegment.Heading(1, "Cached Summary"),
                    MarkdownSegment.Paragraph("Temperature: 20°C"),
                    MarkdownSegment.Paragraph("Conditions: Cloudy")
                ),
                llm = "MiniMax M3.1 Flash",
                timeStamp = System.currentTimeMillis() - 3600000,
                networkError = "Network unavailable"
            )
        )
    }
}

@Preview(
    name = "Error State",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun WeatherSummaryErrorPreview() {
    WeatherForecastTheme(darkTheme = true) {
        WeatherSummaryLayout(
            onShare = {},
            onRefresh = {},
            onNavigateUp = {},
            appBarUiState = AppBarUiState(
                titleResId = R.string.app_name,
                titleColorAttr = R.attr.colorInfo,
                subtitle = TextType.Text("Failed to load forecast for New York"),
                subtitleColorAttr = R.attr.colorInfo,
                subtitleSize = SubtitleSize.Small
            ),
            weatherSummaryUiState = WeatherSummaryUiState.Error(
                city = "Nizhny Novgorod",
                messageId = R.string.error_host_unreachable
            )
        )
    }
}