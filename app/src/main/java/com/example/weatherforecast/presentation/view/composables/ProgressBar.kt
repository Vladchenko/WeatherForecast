package com.example.weatherforecast.presentation.view.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Displays a full-screen semi-transparent overlay with a loading spinner.
 *
 * Uses a white background with 30% opacity to dim the underlying content.
 */
@Composable
fun ProgressBar() {
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(16.dp))
            .fillMaxSize()
            .alpha(0.3f)
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}