package io.github.vladchenko.weatherforecast.core.ui.component

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Loading skeleton indicator with shimmer effect.
 *
 * Displays an animated gradient shimmer effect used
 * as a placeholder during content loading.
 *
 * @param modifier Modifier to apply to the component.
 * @param shimmerColors Shimmer effect color configuration.
 */
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    shimmerColors: ShimmerColors = ShimmerDefaults.colors()
) {
    val shimmerBrush = rememberShimmerBrush(shimmerColors = shimmerColors)
    ShimmerBox(
        modifier = modifier
            .fillMaxSize(),
        brush = shimmerBrush,
        shape = RoundedCornerShape(8.dp)
    )
}

/**
 * Base component for displaying the shimmer effect.
 *
 * @param modifier Modifier to apply to the box.
 * @param brush Brush used for the shimmer gradient.
 * @param shape Shape of the shimmer box (rounded corners by default).
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
 * Creates a brush with animated shimmer effect.
 *
 * @param shimmerColors Shimmer effect color configuration.
 * @return Linear gradient brush animated to simulate shimmer.
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
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerProgress"
    )

    return Brush.linearGradient(
        colors = listOf(
            shimmerColors.baseColor,
            shimmerColors.highlightColor,
            shimmerColors.baseColor
        ),
        start = Offset(
            x = shimmerProgress * 1500f - 500f,
            y = shimmerProgress * 1500f - 160f
        ),
        end = Offset(
            x = shimmerProgress * 1500f + 50f,
            y = shimmerProgress * 1500f + 160f
        )
    )
}

/**
 * Configuration class for shimmer effect colors and animation duration.
 *
 * @property baseColor Base background shimmer color.
 * @property highlightColor Highlight color passing across the surface.
 * @property durationMs Duration of one shimmer cycle in milliseconds.
 */
data class ShimmerColors(
    val baseColor: Color,
    val highlightColor: Color,
    val durationMs: Int = 1500
)

/**
 * Object with default shimmer color configurations.
 */
object ShimmerDefaults {
    /**
     * Provides default shimmer colors with override capability.
     *
     * @param baseColor Background shimmer color (default: light gray with low alpha).
     * @param highlightColor Highlight color passing across the surface (default: white with medium alpha).
     * @param durationMs Animation duration in milliseconds.
     * @return Configured instance of [ShimmerColors].
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
 * Preview for [SkeletonLoader] component.
 *
 * Displays the loading state with shimmer effect on a dark background for contrast.
 */
@Preview(
    name = "SkeletonLoader Preview",
    showBackground = true,
    backgroundColor = 0xFF1E1E1E
)
@Composable
private fun SkeletonLoaderPreview() {
    SkeletonLoader(
        modifier = Modifier
            .padding(16.dp),
    )
}
