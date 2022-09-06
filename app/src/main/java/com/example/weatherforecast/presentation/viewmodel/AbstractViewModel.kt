package com.example.weatherforecast.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

/**
 * View model (MVVM component) with common code to other viewModels.
 *
 * @property app custom [Application] required for its successors.
 */
open class AbstractViewModel(private val app: Application) : AndroidViewModel(app) {

    //region livedata fields
    protected val _onShowErrorLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    protected val _onUpdateStatusLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    protected val _onShowProgressBarLiveData: SingleLiveEvent<Boolean> = SingleLiveEvent()
    //endregion livedata fields

    //region livedata getters fields
    val onShowErrorLiveData: LiveData<String>
        get() = _onShowErrorLiveData

    val onUpdateStatusLiveData: LiveData<String>
        get() = _onUpdateStatusLiveData

    val onShowProgressBarLiveData: LiveData<Boolean>
        get() = _onShowProgressBarLiveData
    //endregion livedata getters fields

    /**
     * Update status message.
     */
    fun onUpdateStatus(statusMessage: String) {
        _onUpdateStatusLiveData.postValue(statusMessage)
    }

    /**
     * Update status message.
     */
    fun onShowError(statusMessage: String) {
        _onShowErrorLiveData.postValue(statusMessage)
    }
}