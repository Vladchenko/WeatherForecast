package com.example.weatherforecast.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Internet connection observer
 * https://www.youtube.com/watch?v=wvDPG2iQ-OE
 */
interface ConnectivityObserver {
    /**
     * Internet connection flow.
     * true if connection is available, false otherwise
     */
    val isConnected: Flow<Boolean>
}