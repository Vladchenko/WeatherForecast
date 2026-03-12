package com.example.weatherforecast.presentation.view.fragments.forecast.hourly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.R
import com.example.weatherforecast.models.domain.HourlyItemDomainModel
import com.example.weatherforecast.models.domain.HourlyWeatherDomainModel
import com.example.weatherforecast.presentation.view.fragments.forecast.current.ShowProgressBar
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable function that displays the hourly forecast section.
 *
 * Shows a horizontal scrollable list of hourly weather data including time, temperature,
 * and weather condition. If the provided [hourlyWeather] is null, nothing is rendered.
 *
 * @param statusColor to display status messages with proper color
 * @param hourlyWeather Data model containing a list of hourly forecasts
 */
@Composable
fun HourlyWeatherLayout(
    statusColor: Color,
    hourlyWeather: WeatherUiState<HourlyWeatherDomainModel>?,
) {
    if (hourlyWeather == null) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f)
    ) {
        Text(
            text = stringResource(R.string.forecast_hourly),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
        Box(
            modifier = Modifier
                .height(140.dp)
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            when (hourlyWeather) {
                is WeatherUiState.Error -> {
                    Text(
                        text = hourlyWeather.message,
                        fontSize = 32.sp,
                        color = statusColor,
                        textAlign = TextAlign.Center
                    )
                }
                is WeatherUiState.Loading -> {
                    ShowProgressBar()
                }
                is WeatherUiState.Success -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .mask(
                                brush = horizontalFadeGradient(
                                    startFadeDp = 16,   // 80.dp слева — пустое пространство
                                    endFadeDp = 16,     // 80.dp справа — пустое пространство
                                    fadeWidthDp = 100    // 40.dp — ширина самого затухания
                                )
                            )
                    ) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(hourlyWeather.data.hourlyForecasts) { forecast ->
                                HourlyForecastItem(forecast)
                            }
                        }
                    }
                }
            }
        }
    }

}

/**
 * Private composable that renders a single hourly forecast item as a card.
 *
 * Displays:
 * - Time (formatted from timestamp)
 * - Temperature in degrees Celsius
 * - Weather description (e.g., "clear sky", "light rain")
 *
 * Styled with rounded corners, fixed size, and centered content.
 *
 * @param forecast Domain model containing data for one hour
 */
@Composable
private fun HourlyForecastItem(forecast: HourlyItemDomainModel) {
    Surface(
        modifier = Modifier
            .width(130.dp)
            .height(110.dp)
            .padding(8.dp),
        color = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatTime(forecast.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = forecast.temperature,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = forecast.weatherDescription,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Formats a Unix timestamp into a 24-hour time string (e.g., "14:30").
 *
 * The timestamp is expected to be in seconds, so it's multiplied by 1000 to convert to milliseconds.
 *
 * @param timestamp Unix timestamp in seconds
 * @return Formatted time string in "HH:mm" format using the device's default locale
 */
private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}

fun Modifier.mask(brush: Brush) = this.then(
    Modifier
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithCache {
            onDrawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.DstIn)
            }
        }
)

@Composable
fun horizontalFadeGradient(
    startFadeDp: Int = 60,   // отступ слева до начала fade
    endFadeDp: Int = 60,     // отступ справа до конца fade
    fadeWidthDp: Int = 40     // ширина самой зоны затухания
): Brush {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenWidthPx = with(density) { screenWidthDp.dp.toPx() }
    val startFadePx = with(density) { startFadeDp.dp.toPx() }
    val endFadePx = with(density) { endFadeDp.dp.toPx() }
    val fadeWidthPx = with(density) { fadeWidthDp.dp.toPx() }

    // Начало левого градиента
    val leftStart = startFadePx / screenWidthPx
    // Конец левого градиента (где контент становится полностью видимым)
    val leftEnd = (startFadePx + fadeWidthPx) / screenWidthPx

    // Начало правого градиента (где контент начинает исчезать)
    val rightStart = (screenWidthPx - endFadePx - fadeWidthPx) / screenWidthPx
    // Конец правого градиента
    val rightEnd = (screenWidthPx - endFadePx) / screenWidthPx

    return Brush.horizontalGradient(
        0f to Color.Transparent,
        leftStart to Color.Transparent,
        leftEnd to Color.Black,
        rightStart to Color.Black,
        rightEnd to Color.Transparent,
        1f to Color.Transparent
    )
}