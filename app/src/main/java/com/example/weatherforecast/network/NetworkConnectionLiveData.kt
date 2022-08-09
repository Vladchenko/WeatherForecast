package com.example.weatherforecast.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.weatherforecast.R

/**
 * Notifies through a broadcast receiver about a network availability.
 */
class NetworkConnectionLiveData(private val context: Context) : LiveData<Boolean>() {

    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    private val networkCallback: ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                Log.d("NetworkConnectionLiveData", context.getString(R.string.network_available_text))
                super.onAvailable(network)
                postValue(true)
            }

            override fun onLost(network: Network) {
                Log.d("NetworkConnectionLiveData", context.getString(R.string.network_not_available_error_text))
                super.onLost(network)
                postValue(false)
            }
        }

    override fun onActive() {
        super.onActive()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onInactive() {
        super.onInactive()
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}