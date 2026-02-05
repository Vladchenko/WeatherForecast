package com.example.weatherforecast.utils

import androidx.annotation.StringRes

/**
 * Resource manager to provide string resources
 */
interface ResourceManager {
    /**
     * Get string resource, using [resId] as key
     */
    fun getString(@StringRes resId: Int): String

    /**
     * Get string resource, using [resId] as key and [formatArgs] as format arguments
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
}
