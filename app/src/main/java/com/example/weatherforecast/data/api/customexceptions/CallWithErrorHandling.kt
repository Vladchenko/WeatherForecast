package com.example.weatherforecast.data.api.customexceptions

import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Invocation
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * A wrapper for Retrofit Call that provides custom error handling for network requests.
 * This class handles common network errors and maps them to appropriate domain exceptions.
 *
 * @property delegate The original Retrofit Call to be wrapped
 */
class CallWithErrorHandling(
    private val delegate: Call<Any>
) : Call<Any> by delegate {

    override fun enqueue(callback: Callback<Any>) {
        delegate.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful) {
                    callback.onResponse(call, response)
                } else {
                    val exception = mapExceptionOfCall(call, HttpException(response))
                    callback.onFailure(call, exception)
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                val exception = mapExceptionOfCall(call, t)
                callback.onFailure(call, exception)
            }
        })
    }

    /**
     * Maps network exceptions to domain exceptions using custom mappers if available.
     *
     * @param call The Retrofit call that failed
     * @param t The throwable that caused the failure
     * @return A mapped domain exception
     */
    private fun mapExceptionOfCall(call: Call<Any>, t: Throwable): Exception {
        val retrofitInvocation = call.request().tag(Invocation::class.java)
        val annotation = retrofitInvocation?.method()?.getAnnotation(ExceptionsMapper::class.java)
        
        val mapper = try {
            annotation?.value?.java?.constructors?.first()
                ?.newInstance(retrofitInvocation.arguments()) as? HttpExceptionMapper
        } catch (e: Exception) {
            null
        }

        return when (t) {
            is HttpException -> {
                mapper?.map(t) ?: WeatherException("HTTP Error: ${t.code()} - ${t.message()}")
            }
            is SocketTimeoutException -> {
                WeatherException("Connection timeout. Please check your internet connection.")
            }
            is IOException -> {
                WeatherException("Network error. Please check your internet connection.")
            }
            else -> {
                WeatherException("An unexpected error occurred: ${t.message}")
            }
        }
    }

    override fun clone() = CallWithErrorHandling(delegate.clone())

    companion object {
        private const val TAG = "CallWithErrorHandling"
    }
}