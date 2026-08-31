package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    shimmerColors: ShimmerColors = ShimmerDefaults.colors()
) {
    val shimmerBrush = rememberShimmerBrush(shimmerColors = shimmerColors)
    ShimmerBox(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        brush = shimmerBrush,
        shape = RoundedCornerShape(4.dp)
    )
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
            x = shimmerProgress * 1500f - 500f,
            y = shimmerProgress * 1500f - 160f
        ),
        end = androidx.compose.ui.geometry.Offset(
            x = shimmerProgress * 1500f + 50f,
            y = shimmerProgress * 1500f + 160f
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
    CurrentWeatherSkeleton()
}