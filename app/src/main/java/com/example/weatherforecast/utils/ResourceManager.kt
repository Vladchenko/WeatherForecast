package com.example.weatherforecast.utils

import android.content.Context
import androidx.annotation.StringRes
import javax.inject.Inject

/**
 * Resource manager to provide string resources
 */
class ResourceManager @Inject constructor(
    private val context: Context
) {
    /**
     * Get string resource, using [resId] as key
     */
    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    /**
     * Get string resource, using [resId] as key and [formatArgs] as format arguments
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}