package com.example.weatherforecast.models.presentation

import androidx.annotation.AttrRes
import com.example.weatherforecast.R


/**
 * Model for the app bar state.
 *
 * @property title of the app bar
 * @property subtitle of the app bar
 * @property subtitleColorAttr for the subtitle of the app bar
 * @property isVisible whether the app bar is visible
 * @property actionsVisible whether the actions are visible
 * @property navigationIconVisible whether the navigation icon is visible
 */
data class AppBarState(
    val title: String = "",
    val subtitle: String = "",
    @param:AttrRes val subtitleColorAttr: Int = R.attr.colorInfo,
    val isVisible: Boolean = true,
    val actionsVisible: Boolean = true,
    val navigationIconVisible: Boolean = true
)