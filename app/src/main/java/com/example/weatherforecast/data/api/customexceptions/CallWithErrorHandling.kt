package com.example.weatherforecast.data.api.customexceptions

import retrofit2.*
import java.io.IOException

/**
 * Provides custom error handling for network requests.
 *
 * @property delegate to send network request.
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
                    callback.onFailure(call, mapExceptionOfCall(call, HttpException(response)))
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                callback.onFailure(call, mapExceptionOfCall(call, t))
            }
        })
    }

    fun mapExceptionOfCall(call: Call<Any>, t: Throwable): Exception {
        val retrofitInvocation = call.request().tag(Invocation::class.java)
        val annotation = retrofitInvocation?.method()?.getAnnotation(ExceptionsMapper::class.java)
        val mapper = try {
            annotation?.value?.java?.constructors?.first()
                ?.newInstance(retrofitInvocation.arguments()) as HttpExceptionMapper
        } catch (e: Exception) {
            null
        }
        return mapToDomainException(t, mapper)
    }

    override fun clone() = CallWithErrorHandling(delegate.clone())

    private fun mapToDomainException(
        remoteException: Throwable,
        httpExceptionsMapper: HttpExceptionMapper? = null
    ): Exception {
        return when (remoteException) {
            is NoSuchDatabaseEntryException -> NoSuchDatabaseEntryException(remoteException.message.orEmpty())
            is IOException -> NoInternetException(remoteException.message.orEmpty())
            is HttpException -> httpExceptionsMapper?.map(remoteException) ?: ApiException(remoteException.code().toString())
            else -> UnexpectedException(remoteException as java.lang.Exception)
        }
    }
}