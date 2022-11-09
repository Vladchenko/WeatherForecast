package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_DEFAULT_FONT_SIZE
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_ERROR_FONT_COLOR
import com.example.weatherforecast.presentation.PresentationConstants.APPBAR_SUBTITLE_STATUS_FONT_COLOR
import com.example.weatherforecast.presentation.PresentationUtils.getToolbarSubtitleFontSize

/**
 * View model (MVVM component) with common code to other viewModels.
 *
 * @property app custom [Application] required for its successors.
 */
open class AbstractViewModel(private val app: Application) : AndroidViewModel(app) {

    val showProgressBarState: MutableState<Boolean> =  mutableStateOf(true)
    val toolbarSubtitleState: MutableState<String> =  mutableStateOf("")
    val toolbarSubtitleColorState: MutableState<Color> =  mutableStateOf(Color.Unspecified)
    val toolbarSubtitleFontSizeState: MutableState<Int> =  mutableStateOf(APPBAR_SUBTITLE_DEFAULT_FONT_SIZE)

    //region livedata fields
    protected val _onShowErrorLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    protected val _onShowStatusLiveData: SingleLiveEvent<String> = SingleLiveEvent()
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
     * Show error message.
     */
    fun onShowError(errorMessage: String) {
        toolbarSubtitleFontSizeState.value = getToolbarSubtitleFontSize(errorMessage)
        toolbarSubtitleColorState.value = APPBAR_SUBTITLE_ERROR_FONT_COLOR
        toolbarSubtitleState.value = errorMessage
        Log.e("AbstractViewModel", errorMessage)
    }
}