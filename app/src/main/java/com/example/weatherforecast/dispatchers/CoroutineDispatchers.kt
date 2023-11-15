package com.example.weatherforecast.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Dispatchers for coroutines
 */
interface CoroutineDispatchers {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}