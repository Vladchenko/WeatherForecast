package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_DEFAULT_FONT_SIZE
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_ERROR_FONT_COLOR
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_STATUS_FONT_COLOR
import com.example.weatherforecast.presentation.PresentationUtils.getToolbarSubtitleFontSize
import kotlinx.coroutines.launch

/**
 * View model (MVVM component) with code common to inherent viewModels.
 *
 * @property app custom [Application] required for its successors.
 * @property coroutineDispatchers dispatchers for coroutines
 */
open class AbstractViewModel(private val app: Application,
                             private val coroutineDispatchers: CoroutineDispatchers
) : AndroidViewModel(app) {

    val showProgressBarState: MutableState<Boolean> = mutableStateOf(true)
    val toolbarSubtitleTextState: MutableState<String> = mutableStateOf("")
    val toolbarSubtitleColorState: MutableState<Color> = mutableStateOf(Color.Unspecified)
    val toolbarSubtitleFontSizeState: MutableState<Int> = mutableStateOf(APPBAR_SUBTITLE_DEFAULT_FONT_SIZE)

    /**
     * Show [statusMessage].
     */
    fun showStatus(statusMessage: String) {
        toolbarSubtitleFontSizeState.value = getToolbarSubtitleFontSize(statusMessage)
        toolbarSubtitleColorState.value = APPBAR_SUBTITLE_STATUS_FONT_COLOR
        toolbarSubtitleTextState.value = statusMessage
        Log.d("AbstractViewModel", statusMessage)
    }

    /**
     * Show status message, providing [stringResId].
     */
    fun showStatus(@StringRes stringResId: Int) {
        viewModelScope.launch(coroutineDispatchers.main) {
            showStatus(app.getString(stringResId))
        }
    }

    /**
     * Show status message, providing [stringResId] and [value] as argument.
     */
    fun showStatus(@StringRes stringResId: Int, value: String) {    // TODO varargs are not displayed correctly
        viewModelScope.launch(coroutineDispatchers.main) {
            showStatus(app.getString(stringResId, value))
        }
    }

    /**
     * Show [errorMessage].
     */
    fun showError(errorMessage: String) {
        toolbarSubtitleFontSizeState.value = getToolbarSubtitleFontSize(errorMessage)
        toolbarSubtitleColorState.value = APPBAR_SUBTITLE_ERROR_FONT_COLOR
        toolbarSubtitleTextState.value = errorMessage
        Log.e("AbstractViewModel", errorMessage)
    }

    /**
     * Show error message, providing [stringResId].
     */
    fun showError(@StringRes stringResId: Int) {
        viewModelScope.launch(coroutineDispatchers.main) {
            showError(app.getString(stringResId))
        }
    }

    /**
     * Show error message, providing [stringResId] and [value] as argument.
     */
    fun showError(@StringRes stringResId: Int, value: String) {
        viewModelScope.launch(coroutineDispatchers.main) {
            showError(app.getString(stringResId, value))
        }
    }
}