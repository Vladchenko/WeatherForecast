package com.example.weatherforecast.models.domain

sealed class LoadResult<T> {
    data class Remote<T>(val data:T): LoadResult<T>()
    data class Local<T>(val data:T, val remoteError: String): LoadResult<T>()
    data class Fail<T>(val exception: Exception): LoadResult<T>()
}