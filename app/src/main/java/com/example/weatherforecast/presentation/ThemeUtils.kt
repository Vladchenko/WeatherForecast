package com.example.weatherforecast.presentation

import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun themeColor(@AttrRes attrRes: Int): Color {
    val context = LocalContext.current
    val typedValue = remember { TypedValue() }
    return if (context.theme.resolveAttribute(attrRes, typedValue, true)) {
        Color(typedValue.data)
    } else {
        Color.Black
    }
}