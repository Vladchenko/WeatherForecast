package com.example.weatherforecast.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.weatherforecast.data.util.TemperatureType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFERENCES_NAME = "app_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

/**
 * Manages app preferences using DataStore.
 *
 * Provides reactive access to user settings, currently supporting temperature unit.
 * Value is persisted and emits immediately on subscription.
 *
 * @property context for DataStore access
 * @property coroutineScope for flow collection
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    /**
     * Flow of user's preferred temperature unit. Emits current value immediately.
     * Default: [TemperatureType.CELSIUS].
     */
    val temperatureTypeStateFlow: StateFlow<TemperatureType> = context.dataStore.data
        .map { preferences ->
            when (preferences[TEMPERATURE_UNIT]) {
                "KELVIN" -> TemperatureType.KELVIN
                "FAHRENHEIT" -> TemperatureType.FAHRENHEIT
                "CELSIUS" -> TemperatureType.CELSIUS
                else -> TemperatureType.CELSIUS
            }
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TemperatureType.CELSIUS
        )

    companion object {
        private val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    }
}