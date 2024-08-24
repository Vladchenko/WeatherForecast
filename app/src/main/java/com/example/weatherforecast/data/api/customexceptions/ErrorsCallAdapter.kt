package com.example.weatherforecast.data.api.customexceptions

import retrofit2.Call
import retrofit2.CallAdapter

/**
 * Adapter to process network requests.
 *
 * @property delegateAdapter to delegate network requests to.
 */
class ErrorsCallAdapter(
    private val delegateAdapter: CallAdapter<Any, Call<*>>
) : CallAdapter<Any, Call<*>> by delegateAdapter {

    override fun adapt(call: Call<Any>): Call<*> {
        return delegateAdapter.adapt(CallWithErrorHandling(call))
    }
}