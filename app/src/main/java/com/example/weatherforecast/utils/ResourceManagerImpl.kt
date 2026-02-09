package com.example.weatherforecast.utils

import android.content.Context
import android.content.res.Resources
import androidx.annotation.StringRes
import javax.inject.Inject

/**
 * Implementation of [ResourceManager]
 *
 * @property context to get android-specific resources
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

    override fun getResources(): Resources {
        return context.resources
    }

    override fun getPackageName(): String {
        return context.packageName
    }
}