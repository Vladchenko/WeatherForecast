package com.example.weatherforecast.utils

import android.content.res.Resources
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

    /**
     * Get resources object
     */
    fun getResources(): Resources

    /**
     * Get package name of the application
     */
    fun getPackageName(): String
}
