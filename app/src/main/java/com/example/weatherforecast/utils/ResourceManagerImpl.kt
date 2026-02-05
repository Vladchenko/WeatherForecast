package com.example.weatherforecast.utils

import android.content.Context
import androidx.annotation.StringRes
import javax.inject.Inject

/**
 * Implementation of [ResourceManager]
 */
class ResourceManagerImpl @Inject constructor(
    private val context: Context
): ResourceManager {

    override fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}