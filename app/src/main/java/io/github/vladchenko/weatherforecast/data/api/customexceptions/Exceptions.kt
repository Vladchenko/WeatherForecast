package io.github.vladchenko.weatherforecast.data.api.customexceptions

/**
 * Exception indicating there is no such entry
 *
 * @param entryName name of the database entry that is absent.
 */
class NoSuchDatabaseEntryException(entryName: String) : Exception(entryName)