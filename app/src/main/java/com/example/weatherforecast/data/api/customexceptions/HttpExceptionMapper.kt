package com.example.weatherforecast.data.api.customexceptions

import retrofit2.HttpException

/**
 * Mapper to map custom exceptions
 *
 * @property callArguments for remote requests.
 */
abstract class HttpExceptionMapper(protected val callArguments: List<String>) {
    abstract fun map(httpException: HttpException): Exception?
}