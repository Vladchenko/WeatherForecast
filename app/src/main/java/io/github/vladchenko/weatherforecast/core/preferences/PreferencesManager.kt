package io.github.vladchenko.weatherforecast.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit
import io.github.vladchenko.weatherforecast.feature.aiweathersummary.domain.models.AiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Manages app settings using DataStore.
 *
 * Provides reactive access to user preferences, currently supporting temperature unit.
 * The value is saved and emitted immediately upon subscription.
 *
 * @property context context for accessing DataStore
 * @property coroutineScope scope for collecting flows
 */
class PreferencesManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    /**
     * Flow of the user's preferred temperature unit. Emits the current value immediately.
     * Default value: [TemperatureUnit.CELSIUS].
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
     * Sets the user's preferred temperature unit and saves it to DataStore.
     *
     * @param unit temperature unit to save (Celsius, Fahrenheit, or Kelvin)
     */
    fun setTemperatureUnit(unit: TemperatureUnit) {
        coroutineScope.launch {
            context.dataStore.edit { prefs ->
                prefs[TEMPERATURE_UNIT] = unit.name
            }
        }
    }

    /**
     * Flow of the user's preferred AI model for weather summary. Emits the current value immediately.
     * Default value: [AiModel.default].
     */
    val aiModel: StateFlow<AiModel> = context.dataStore.data
        .map { preferences ->
            AiModel.all.firstOrNull { it.id == preferences[AiModelPreference] }
                ?: AiModel.default
        }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = AiModel.default
        )

    /**
     * Sets the user's preferred AI model and saves it to DataStore.
     *
     * @param model AI model to save
     */
    fun setAiModel(model: AiModel) {
        coroutineScope.launch {
            context.dataStore.edit { prefs ->
                prefs[AiModelPreference] = model.id
            }
        }
    }

    companion object {
        /**
         * Name of the DataStore preferences file.
         */
        private const val PREFERENCES_NAME = "app_preferences"

        /**
         * Key for storing the temperature unit.
         */
        private val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")

        /**
         * Key for storing the AI model.
         */
        private val AiModelPreference = stringPreferencesKey("ai_model")

        /**
         * Extension to obtain DataStore from Context.
         */
        private val Context.dataStore: DataStore<Preferences>
                by preferencesDataStore(name = PREFERENCES_NAME)
    }
}