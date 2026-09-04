package io.github.vladchenko.weatherforecast.core.resourcemanager

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import javax.inject.Inject

/**
 * Implementation of [ResourceManager]
 *
 * @property context to get android-specific resources
 */
class ResourceManagerImpl @Inject constructor(
    private val context: Context
): ResourceManager {

    override fun getThemeColorRes(@AttrRes attrResId: Int): Int {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(attrResId, typedValue, true)) {
            typedValue.resourceId.takeIf { it != 0 } ?: typedValue.data
        } else {
            android.R.color.black
        }
    }
}