package io.github.vladchenko.weatherforecast.models.presentation

import androidx.annotation.AttrRes
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.ui.constants.SubtitleSize

/**
 * Model for the app bar state.
 *
 * @property title of the app bar
 * @property subtitle of the app bar
 * @property subtitleSize of the app bar
 * @property titleColorAttr for the title of the app bar
 * @property subtitleColorAttr for the subtitle of the app bar
 * @property isVisible whether the app bar is visible
 * @property actionsVisible whether the actions are visible
 * @property navigationIconVisible whether the navigation icon is visible
 */
data class AppBarState(
    val title: String = "",
    val subtitle: String = "",
    val subtitleSize: SubtitleSize = SubtitleSize.Normal,
    @param:AttrRes val titleColorAttr: Int = R.attr.colorInfo,
    @param:AttrRes val subtitleColorAttr: Int = R.attr.colorInfo,
    val isVisible: Boolean = true,
    val actionsVisible: Boolean = true,
    val navigationIconVisible: Boolean = true
)