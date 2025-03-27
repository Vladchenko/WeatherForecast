package com.example.weatherforecast.data.util

import retrofit2.Response
import javax.inject.Inject

class ResponseProcessor @Inject constructor() {
    fun <T> processResponse(response: Response<T>): Response<T> {
        if (!response.isSuccessful) {
            throw Exception("API call failed with code: ${response.code()}")
        }
        return response
    }
} 