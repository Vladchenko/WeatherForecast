package com.example.weatherforecast.data.api.customexceptions

/**
 * Exception raised during communication with the remote api. Contains http [statusCode].
 *
 * @property statusCode to define the reason of problem.
 */
data class ApiException(val statusCode: String) : Exception()

/**
 * Exception indicating that device is not connected to the internet.
 *
 * @param [message].
 */
class NoInternetException(message: String) : Exception(message)

/**
 * Exception indicating network timeout occurred.
 *
 * @param message error message
 */
class NetworkTimeoutException(message: String) : Exception(message)

/**
 * Exception indicating there is no such entry
 *
 * @param entryName name of the database entry that is absent.
 */
class NoSuchDatabaseEntryException(entryName: String) : Exception(entryName)

/**
 * Not handled unexpected exception.
 *
 * @param cause of the exception.
 */
class UnexpectedException(cause: Exception) : Exception(cause)

/**
 * Exception indicating geo location is not available.
 *
 * @constructor to provide dependencies
 *
 * @param cause of the exception.
 */
class GeoLocationException(cause: Exception) : Exception(cause)