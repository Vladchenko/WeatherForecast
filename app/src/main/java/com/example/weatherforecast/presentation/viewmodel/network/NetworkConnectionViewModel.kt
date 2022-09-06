package com.example.weatherforecast.presentation.viewmodel.network

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.weatherforecast.R
import com.example.weatherforecast.network.NetworkConnectionListener
import com.example.weatherforecast.network.NetworkUtils
import com.example.weatherforecast.presentation.viewmodel.AbstractViewModel
import com.example.weatherforecast.presentation.viewmodel.SingleLiveEvent

/**
 * View model that notifies about network connection availability.
 *
 * @property app custom [Application] implementation for Hilt.
 */
class NetworkConnectionViewModel(private val app: Application) : AbstractViewModel(app), NetworkConnectionListener {

    val onNetworkConnectionAvailableLiveData: LiveData<Unit>
        get() = _onNetworkConnectionAvailableLiveData
    val onNetworkConnectionLostLiveData: LiveData<Unit>
        get() = _onNetworkConnectionLostLiveData

    private val _onNetworkConnectionAvailableLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _onNetworkConnectionLostLiveData: SingleLiveEvent<Unit> = SingleLiveEvent()

    override fun onNetworkConnectionAvailable() {
        Log.d("NetworkConnectionViewModel","onNetworkConnectionAvailable")
        _onUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.network_available_text))
        _onNetworkConnectionAvailableLiveData.postValue(Unit)
    }

    override fun onNetworkConnectionLost() {
        Log.d("NetworkConnectionViewModel","onNetworkConnectionLost")
        _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
        _onNetworkConnectionLostLiveData.postValue(Unit)
    }

    /**
     * Check if network is available.
     */
    fun checkNetworkAvailability() {
        Log.d("NetworkConnectionViewModel","No network connection")
        if (!NetworkUtils.isNetworkAvailable(app.applicationContext)) {
            _onShowErrorLiveData.postValue(app.getString(R.string.network_not_available_error_text))
        }
    }
}