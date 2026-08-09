package com.resurface.resurface.data.wristband

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.resurface.resurface.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferências dedicadas da pulseira (BLE é domínio à parte, D-3). Guarda só a intensidade
 * do pulso do aviso. Null = posição "auto": omite o byte e o firmware aplica o padrão dele.
 */
@Singleton
class WristbandPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private object Keys {
        val INTENSITY = intPreferencesKey("wristband_intensity")
    }

    /** Intensidade gravada (0–255) ou null se nunca configurada (auto). */
    val intensity: Flow<Int?> = dataStore.data.map { it[Keys.INTENSITY] }

    /** Grava a intensidade; null limpa (volta ao auto). */
    suspend fun setIntensity(value: Int?) = withContext(io) {
        dataStore.edit { prefs ->
            if (value == null) prefs.remove(Keys.INTENSITY) else prefs[Keys.INTENSITY] = value.coerceIn(0, 255)
        }
    }
}
