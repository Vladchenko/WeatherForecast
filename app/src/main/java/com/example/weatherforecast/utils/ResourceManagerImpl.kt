package com.example.weatherforecast.utils

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AttrRes
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

    override fun getColor(color: Int): Int {
        return context.getColor(color)
    }

    override fun getThemeColorRes(@AttrRes attrResId: Int): Int {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(attrResId, typedValue, true)) {
            typedValue.resourceId.takeIf { it != 0 } ?: typedValue.data
        } else {
            android.R.color.black
        }
    }
}