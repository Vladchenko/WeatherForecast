package com.example.weatherforecast.data.api.customexceptions

/**
 * Exception when communicating with the remote api. Contains http [statusCode].
 */
data class ApiException(val statusCode: String) : Exception()

/**
 * Exception indicating that device is not connected to the internet
 */
class NoInternetException : Exception()

/**
 * Not handled unexpected exception
 */
class UnexpectedException(cause: Exception) : Exception(cause)