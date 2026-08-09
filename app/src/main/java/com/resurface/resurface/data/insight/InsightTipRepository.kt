package com.resurface.resurface.data.insight

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.resurface.resurface.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persiste o índice rotativo do tip (avança 1x por launch) e o cache das reescritas da IA por dia.
 * O cache é um mapa `factKey -> texto` válido só pro [dateKey] guardado; muda o dia, zera. Mantém
 * a rede em no máximo uma chamada por fato distinto por dia (D-6 análogo).
 */
@Singleton
class InsightTipRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private object Keys {
        val ROTATION = intPreferencesKey("tip_rotation")
        val CACHE_DATE = stringPreferencesKey("tip_cache_date")
        val CACHE_JSON = stringPreferencesKey("tip_cache_json")
    }

    private val json = Json { ignoreUnknownKeys = true }

    /** Lê o índice atual e persiste +1 (uma vez por launch). */
    suspend fun nextRotationIndex(): Int = withContext(io) {
        val current = dataStore.data.first()[Keys.ROTATION] ?: 0
        dataStore.edit { it[Keys.ROTATION] = current + 1 }
        current
    }

    /** Texto de IA cacheado pra [factKey] no [dateKey], ou null (dia diferente ou ausente). */
    suspend fun cachedText(dateKey: String, factKey: String): String? = withContext(io) {
        val prefs = dataStore.data.first()
        if (prefs[Keys.CACHE_DATE] != dateKey) return@withContext null
        decode(prefs[Keys.CACHE_JSON])[factKey]
    }

    /** Guarda o texto de IA pra [factKey]; reinicia o mapa se o dia mudou. */
    suspend fun cache(dateKey: String, factKey: String, text: String) = withContext(io) {
        dataStore.edit { prefs ->
            val sameDay = prefs[Keys.CACHE_DATE] == dateKey
            val map = if (sameDay) decode(prefs[Keys.CACHE_JSON]).toMutableMap() else mutableMapOf()
            map[factKey] = text
            prefs[Keys.CACHE_DATE] = dateKey
            prefs[Keys.CACHE_JSON] = json.encodeToString(map as Map<String, String>)
        }
    }

    private fun decode(raw: String?): Map<String, String> =
        if (raw.isNullOrBlank()) emptyMap()
        else runCatching { json.decodeFromString<Map<String, String>>(raw) }.getOrDefault(emptyMap())
}
