package com.example.weatherforecast.data.util

import android.util.Log
import javax.inject.Inject

class LoggingService @Inject constructor() {
    fun logApiResponse(tag: String, message: String, response: Any?) {
        Log.d(tag, "$message follows next")
        Log.d(tag, response.toString())
    }
} 