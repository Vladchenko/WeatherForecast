package io.github.vladchenko.weatherforecast.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import io.github.vladchenko.weatherforecast.R

/**
 * Displays the full-screen background image.
 *
 * The image covers the entire screen and respects scaffold padding.
 */
@Composable
 fun BackgroundImage() {
    Image(
        painter = painterResource(id = R.drawable.background2),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize(),
    )
}