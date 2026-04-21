package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp

/**
 * Skeleton loader for the main content of CurrentWeatherLayout with shimmer effect.
 */
@Composable
fun CurrentWeatherSkeleton(
    modifier: Modifier = Modifier,
    shimmerColors: ShimmerColors = ShimmerDefaults.colors()
) {
    val shimmerBrush = rememberShimmerBrush(shimmerColors = shimmerColors)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date and time
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(18.dp),
            brush = shimmerBrush,
            shape = RoundedCornerShape(4.dp)
        )

        // City (largest element)
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(top = 28.dp)
                .height(30.dp),
            brush = shimmerBrush,
            shape = RoundedCornerShape(4.dp)
        )
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(top = 10.dp)
                .height(30.dp),
            brush = shimmerBrush,
            shape = RoundedCornerShape(4.dp)
        )

        // Temperature and weather icon
        Row(
            modifier = Modifier
                .padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // Temperature (reduced size)
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(60.dp),
                    brush = shimmerBrush,
                    shape = RoundedCornerShape(4.dp)
                )

                // Degree symbol
                ShimmerBox(
                    modifier = Modifier
                        .width(10.dp)
                        .height(6.dp)
                        .padding(start = 4.dp),
                    brush = shimmerBrush,
                    shape = RoundedCornerShape(4.dp)
                )

                // "C" or "F" placeholder
                ShimmerBox(
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .padding(start = 4.dp),
                    brush = shimmerBrush,
                    shape = RoundedCornerShape(4.dp)
                )
            }

            // Weather icon placeholder
            ShimmerBox(
                modifier = Modifier
                    .size(60.dp)
                    .padding(start = 16.dp),
                brush = shimmerBrush,
                shape = RoundedCornerShape(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Weather description
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(14.dp),
            brush = shimmerBrush,
            shape = RoundedCornerShape(4.dp)
        )
    }
}

/**
 * Base component for displaying a shimmer effect.
 *
 * @param modifier The modifier to apply to the box.
 * @param brush The brush used for the shimmer gradient.
 * @param shape The shape of the shimmer box (rounded corners by default).
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
 * Creates a brush with a shimmer animation effect.
 *
 * @param shimmerColors The color configuration for the shimmer effect.
 * @return A linear gradient brush animated to simulate shimmer.
 */
@Composable
private fun rememberShimmerBrush(
    shimmerColors: ShimmerColors
): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
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
        label = "shimmerProgress"
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
 * Configuration class for shimmer effect colors and animation duration.
 *
 * @property baseColor The base background color of the shimmer.
 * @property highlightColor The highlighted color that sweeps across.
 * @property durationMs Duration of one shimmer cycle in milliseconds.
 */
data class ShimmerColors(
    val baseColor: Color,
    val highlightColor: Color,
    val durationMs: Int = 1500
)

/**
 * Default shimmer color configurations.
 */
object ShimmerDefaults {
    /**
     * Provides default shimmer colors with optional overrides.
     *
     * @param baseColor Background color of the shimmer (default: light gray with low alpha).
     * @param highlightColor The moving highlight color (default: white with medium alpha).
     * @param durationMs Animation duration in milliseconds.
     * @return Configured [ShimmerColors] instance.
     */
    @Composable
    fun colors(
        baseColor: Color = Color.White.copy(alpha = 0.1f),
        highlightColor: Color = Color.White.copy(alpha = 0.4f),
        durationMs: Int = 1500
    ): ShimmerColors {
        return ShimmerColors(
            baseColor = baseColor,
            highlightColor = highlightColor,
            durationMs = durationMs
        )
    }
}

/**
 * Preview of the [CurrentWeatherSkeleton] composable.
 * Displays the shimmer loading state with dark background for contrast.
 */
@Preview(
    name = "CurrentWeatherSkeleton Preview",
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
private fun CurrentWeatherSkeletonPreview() {
    CurrentWeatherSkeleton(
        modifier = Modifier.padding(16.dp)
    )
}