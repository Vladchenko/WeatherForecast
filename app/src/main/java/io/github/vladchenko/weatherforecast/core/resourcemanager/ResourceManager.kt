package io.github.vladchenko.weatherforecast.core.resourcemanager

import androidx.annotation.AttrRes

/**
 * Resource manager to provide string resources
 */
@Deprecated("Compose gets strings, colors and other resources on its own")
interface ResourceManager {

    /**
     * Get theme color resource id, using [attrResId] as key
     */
    fun getThemeColorRes(@AttrRes attrResId: Int): Int

}
