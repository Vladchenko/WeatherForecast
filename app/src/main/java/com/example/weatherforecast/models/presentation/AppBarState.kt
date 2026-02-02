package com.example.weatherforecast.models.presentation

import androidx.compose.ui.graphics.Color

/**
 * Model for the app bar state.
 *
 * @property title of the app bar
 * @property subtitle of the app bar
 * @property subtitleColor for the subtitle of the app bar
 * @property isVisible whether the app bar is visible
 * @property actionsVisible whether the actions are visible
 * @property navigationIconVisible whether the navigation icon is visible
 */
data class AppBarState(
    val title: String = "",
    val subtitle: String = "",
    val subtitleColor: Color = Color.Unspecified,
    val isVisible: Boolean = true,
    val actionsVisible: Boolean = true,
    val navigationIconVisible: Boolean = true
)