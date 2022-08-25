package com.example.weatherforecast.network

/**
 * Network connection availability listener.
 */
interface NetworkConnectionListener {

    /**
     * Callback for network connection available case.
     */
    fun onNetworkConnectionAvailable()

    /**
     * Callback for network connection lost case.
     */
    fun onNetworkConnectionLost()
}