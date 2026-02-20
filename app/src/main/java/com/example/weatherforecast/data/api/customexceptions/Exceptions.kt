package com.example.weatherforecast.data.api.customexceptions

/**
 * Exception indicating there is no such entry
 *
 * @param entryName name of the database entry that is absent.
 */
class NoSuchDatabaseEntryException(entryName: String) : Exception(entryName)

/**
 * Exception indicating geo location is not available.
 *
 * @constructor to provide dependencies
 *
 * @param cause of the exception.
 */
class GeoLocationException(cause: Exception) : Exception(cause)