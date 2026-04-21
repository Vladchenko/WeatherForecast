package io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.view

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Skeleton loader for HourlyWeatherLayout with shimmer effect.
 */
@Composable
fun HourlyWeatherSkeleton(
    itemWidth: Dp = 130.dp,
    itemHeight: Dp = 100.dp,
    itemsCount: Int = 5,
    shimmerColors: HourlyShimmerColors = HourlyShimmerDefaults.colors()
) {
    val shimmerBrush = rememberHourlyShimmerBrush(shimmerColors = shimmerColors)

    LazyRow {
        items(itemsCount) {
            HourlyForecastItemSkeleton(
                width = itemWidth,
                height = itemHeight,
                shimmerBrush = shimmerBrush
            )
        }
    }
}

/**
 * Skeleton for a single HourlyForecastItem.
 */
@Composable
fun HourlyForecastItemSkeleton(
    width: Dp,
    height: Dp,
    shimmerBrush: Brush
) {
    Column(
        modifier = Modifier
            .width(width)
            .height(height)
            .padding(start = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(shimmerBrush),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ShimmerBox(
            modifier = Modifier
                .width(130.dp)
                .height(100.dp),
            brush = shimmerBrush
        )
    }
}

/**
 * Base component to display a shimmer effect.
 */
@Composable
private fun ShimmerBox(
    modifier: Modifier,
    brush: Brush,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .graphicsLayer(alpha = 0.9f)
            .background(brush)
    )
}

/**
 * Creates a brush with shimmer animation effect for HourlyForecast background.
 */
@Composable
private fun rememberHourlyShimmerBrush(
    shimmerColors: HourlyShimmerColors
): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "hourlyShimmer")
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = shimmerColors.durationMs,
                easing = LinearOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "hourlyShimmerProgress"
    )

    return Brush.linearGradient(
        colors = listOf(
            shimmerColors.baseColor,
            shimmerColors.highlightColor,
            shimmerColors.baseColor
        ),
        start = androidx.compose.ui.geometry.Offset(
            x = shimmerProgress * 500f - 30f,
            y = shimmerProgress * 500f - 60f
        ),
        end = androidx.compose.ui.geometry.Offset(
            x = shimmerProgress * 500f + 30f,
            y = shimmerProgress * 500f + 60f
        )
    )
}

/**
 * Color configuration for the shimmer effect in HourlyForecast.
 *
 * @property baseColor Background color of the shimmer.
 * @property highlightColor The moving highlight color.
 * @property durationMs Duration of one shimmer animation cycle in milliseconds.
 */
data class HourlyShimmerColors(
    val baseColor: Color,
    val highlightColor: Color,
    val durationMs: Int = 1500
)

/**
 * Default color settings for shimmer effect in HourlyForecast.
 */
object HourlyShimmerDefaults {
    /**
     * Provides default shimmer colors with optional overrides.
     *
     * @param baseColor Background color (default: light gray with 15% alpha).
     * @param highlightColor Moving highlight color (default: white with 40% alpha).
     * @param durationMs Animation duration in milliseconds.
     * @return Configured [HourlyShimmerColors] instance.
     */
    @Composable
    fun colors(
        baseColor: Color = Color.White.copy(alpha = 0.15f),
        highlightColor: Color = Color.White.copy(alpha = 0.4f),
        durationMs: Int = 1500
    ): HourlyShimmerColors {
        return HourlyShimmerColors(
            baseColor = baseColor,
            highlightColor = highlightColor,
            durationMs = durationMs
        )
    }
}

/**
 * Simplified skeleton for HourlyForecastItem (only the item itself, without header or LazyRow).
 */
@Composable
fun HourlyForecastItemSkeletonSimple(
    width: Dp,
    height: Dp,
    shimmerColors: HourlyShimmerColors = HourlyShimmerDefaults.colors()
) {
    val shimmerBrush = rememberHourlyShimmerBrush(shimmerColors = shimmerColors)

    HourlyForecastItemSkeleton(
        width = width,
        height = height,
        shimmerBrush = shimmerBrush
    )
}

@Preview(
    name = "HourlyWeatherSkeleton Preview",
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
private fun HourlyWeatherSkeletonPreview() {
    HourlyWeatherSkeleton(
        itemWidth = 130.dp,
        itemHeight = 100.dp,
        itemsCount = 5
    )
}

@Preview(
    name = "HourlyForecastItemSkeleton Preview",
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
private fun HourlyForecastItemSkeletonPreview() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        HourlyForecastItemSkeletonSimple(
            width = 130.dp,
            height = 100.dp
        )
    }
}