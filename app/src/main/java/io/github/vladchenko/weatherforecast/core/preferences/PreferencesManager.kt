package io.github.vladchenko.weatherforecast.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

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
     * Default: [TemperatureUnit.CELSIUS].
     */
    val temperatureUnit: StateFlow<TemperatureUnit> = context.dataStore.data
        .map { preferences ->
            when (preferences[TEMPERATURE_UNIT]) {
                "KELVIN" -> TemperatureUnit.KELVIN
                "FAHRENHEIT" -> TemperatureUnit.FAHRENHEIT
                "CELSIUS" -> TemperatureUnit.CELSIUS
                else -> TemperatureUnit.CELSIUS
            }
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = TemperatureUnit.CELSIUS
        )

    /**
     * Sets the user's preferred temperature unit and persists it to DataStore.
     *
     * @param unit the temperature unit to save (Celsius, Fahrenheit, or Kelvin)
     */
    fun setTemperatureUnit(unit: TemperatureUnit) {
        coroutineScope.launch {
            context.dataStore.edit { prefs ->
                prefs[TEMPERATURE_UNIT] = unit.name
            }
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "app_preferences"
        private val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        private val Context.dataStore: DataStore<Preferences>
                by preferencesDataStore(name = PREFERENCES_NAME)
    }
}