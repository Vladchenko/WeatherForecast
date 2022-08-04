package com.example.weatherforecast.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.weatherforecast.network.NetworkUtils.isNetworkAvailable

/**
 * Notifies through a broadcast receiver about a network availability.
 */
class NetworkConnectionLiveData(private val context: Context) : LiveData<Boolean>() {

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            postValue(isNetworkAvailable(context))
        }
    }

    override fun onActive() {
        super.onActive()
        context.registerReceiver(
            networkReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)   //TODO Replace this solution with NetworkCallback https://stackoverflow.com/questions/36421930/connectivitymanager-connectivity-action-deprecated
        )
    }

    override fun onInactive() {
        super.onInactive()
        try {
            context.unregisterReceiver(networkReceiver)
        } catch (e: Exception) {
            Log.e("NetworkConnectionLiveData", "Cannot unregister broadcast receiver.")
        }
    }
}