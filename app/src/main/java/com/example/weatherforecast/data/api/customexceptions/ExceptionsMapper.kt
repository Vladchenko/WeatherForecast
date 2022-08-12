package com.example.weatherforecast.data.api.customexceptions

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ExceptionsMapper(val value: KClass<out HttpExceptionMapper>)
