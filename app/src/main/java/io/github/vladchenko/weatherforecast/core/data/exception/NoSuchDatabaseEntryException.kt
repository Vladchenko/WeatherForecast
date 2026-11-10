package io.github.vladchenko.weatherforecast.core.data.exception

/**
 * Exception indicating there is no such entry
 *
 * @param entryName name of the database entry that is absent.
 */
class NoSuchDatabaseEntryException(entryName: String) : Exception(entryName)
