package io.github.vladchenko.weatherforecast.models.presentation

import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.ui.constants.SubtitleSize
import io.github.vladchenko.weatherforecast.core.ui.status.TextType

/**
 * Model for the app bar ui state.
 *
 * @property titleResId string resource id for the title
 * @property subtitle contains a string or string resource id representation of a subtitle text
 * @property subtitleSize of the app bar
 * @property titleColorAttr for the title of the app bar
 * @property subtitleColorAttr for the subtitle of the app bar
 * @property isVisible whether the app bar is visible
 * @property actionsVisible whether the actions are visible
 * @property navigationIconVisible whether the navigation icon is visible
 */
@Immutable
data class AppBarUiState(
    @param:StringRes val titleResId: Int = -1,
    val subtitle: TextType = TextType.Text(""),
    val subtitleSize: SubtitleSize = SubtitleSize.Normal,
    @param:AttrRes val titleColorAttr: Int = R.attr.colorInfo,
    @param:AttrRes val subtitleColorAttr: Int = R.attr.colorInfo,
    val isVisible: Boolean = true,
    val actionsVisible: Boolean = true,
    val navigationIconVisible: Boolean = true
)