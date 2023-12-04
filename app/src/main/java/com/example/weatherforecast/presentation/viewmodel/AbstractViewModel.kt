package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_DEFAULT_FONT_SIZE
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_ERROR_FONT_COLOR
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_STATUS_FONT_COLOR
import com.example.weatherforecast.presentation.PresentationUtils.getToolbarSubtitleFontSize
import kotlinx.coroutines.launch

/**
 * View model (MVVM component) with common code to other viewModels.
 *
 * @param app custom [Application] required for its successors.
 * @param coroutineDispatchers dispatchers for coroutines
 */
open class AbstractViewModel(private val app: Application,
                             private val coroutineDispatchers: CoroutineDispatchers
) : AndroidViewModel(app) {

    val showProgressBarState: MutableState<Boolean> = mutableStateOf(true)
    val toolbarSubtitleState: MutableState<String> = mutableStateOf("")
    val toolbarSubtitleColorState: MutableState<Color> = mutableStateOf(Color.Unspecified)
    val toolbarSubtitleFontSizeState: MutableState<Int> = mutableStateOf(APPBAR_SUBTITLE_DEFAULT_FONT_SIZE)

    //region livedata fields
    private val _onShowErrorLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    private val _onShowStatusLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    //endregion livedata fields

    //region livedata getters fields
    val onShowErrorLiveData: LiveData<String>
        get() = _onShowErrorLiveData

    val onShowStatusLiveData: LiveData<String>
        get() = _onShowStatusLiveData
    //endregion livedata getters fields

    /**
     * Show status message.
     */
    fun onShowStatus(statusMessage: String) {
        toolbarSubtitleFontSizeState.value = getToolbarSubtitleFontSize(statusMessage)
        toolbarSubtitleColorState.value = APPBAR_SUBTITLE_STATUS_FONT_COLOR
        toolbarSubtitleState.value = statusMessage
        Log.d("AbstractViewModel", statusMessage)
    }

    /**
     * Show status message, providing [stringResId].
     */
    fun onShowStatus(@StringRes stringResId: Int) {
        viewModelScope.launch(coroutineDispatchers.main) {
            onShowStatus(app.getString(stringResId))
        }
    }

    /**
     * Show status message, providing [stringResId] and [value] as argument.
     */
    fun onShowStatus(@StringRes stringResId: Int, value: String) {    // TODO varargs are not displayed correctly
        viewModelScope.launch(coroutineDispatchers.main) {
            onShowStatus(app.getString(stringResId, value))
        }
    }

    /**
     * Show [errorMessage].
     */
    fun onShowError(errorMessage: String) {
        toolbarSubtitleFontSizeState.value = getToolbarSubtitleFontSize(errorMessage)
        toolbarSubtitleColorState.value = APPBAR_SUBTITLE_ERROR_FONT_COLOR
        toolbarSubtitleState.value = errorMessage
        Log.e("AbstractViewModel", errorMessage)
    }

    /**
     * Show error message, providing [stringResId].
     */
    fun onShowError(@StringRes stringResId: Int) {
        viewModelScope.launch(coroutineDispatchers.main) {
            onShowError(app.getString(stringResId))
        }
    }

    /**
     * Show error message, providing [stringResId] and [value] as argument.
     */
    fun onShowError(@StringRes stringResId: Int, value: String) {
        viewModelScope.launch(coroutineDispatchers.main) {
            onShowError(app.getString(stringResId, value))
        }
    }
}