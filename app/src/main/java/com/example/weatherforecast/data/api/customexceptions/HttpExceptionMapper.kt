package com.example.weatherforecast.data.api.customexceptions

import retrofit2.HttpException

abstract class HttpExceptionMapper(protected val callArguments: List<String>) {
    abstract fun map(httpException: HttpException): Exception?
}