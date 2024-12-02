package com.example.weatherforecast.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * ConnectivityObserver implementation. Determines if internet is available or not.
 * https://www.youtube.com/watch?v=wvDPG2iQ-OE
 *
 * @property context to get connectivity manager
 */
class ConnectivityObserverImpl(
    private val context: Context,
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    override val isConnected: Flow<Boolean>
        get() = callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(true)
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    trySend(false)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    trySend(false)
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val connected =
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    trySend(connected)
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)
            // Unregister callback when flow is closed
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
}