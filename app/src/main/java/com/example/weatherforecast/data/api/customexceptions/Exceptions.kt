package com.example.weatherforecast.data.api.customexceptions

/**
 * Exception when communicating with the remote api. Contains http [statusCode].
 */
data class ApiException(val statusCode: String) : Exception()

/**
 * Exception indicating that device is not connected to the internet, with a [message].
 */
class NoInternetException(message: String) : Exception(message)

/**
 * Exception indicating there is no such [entryName] in database.
 */
class NoSuchDatabaseEntryException(entryName: String) : Exception(entryName)

/**
 * Not handled unexpected exception.
 */
class UnexpectedException(cause: Exception) : Exception(cause)