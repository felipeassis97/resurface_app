package com.resurface.resurface.data.config

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.resurface.resurface.di.IoDispatcher
import com.resurface.resurface.domain.model.Config
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persiste e lê a config do usuário (limite + pausar por hoje) e entrega o [Config] do domínio.
 * Fonte única (G3): a UI e o serviço leem daqui. Escrita valida e é atômica (edit{}).
 */
@Singleton
class ConfigRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val io: CoroutineDispatcher,
    private val time: TimeProvider,
    private val midnight: MidnightClock,
) {
    private object Keys {
        val LIMIT = intPreferencesKey("limit_minutes")
        val PAUSED_UNTIL = longPreferencesKey("paused_until")
    }

    /** Limite de minutos gravado, com padrão. */
    val limitMinutes: Flow<Int> = dataStore.data.map { it[Keys.LIMIT] ?: DEFAULT_LIMIT }

    /** O Config de domínio montado a partir do que está gravado. */
    val config: Flow<Config> = limitMinutes.map { Config(limitMinutes = it) }

    /** Se "pausar por hoje" está ativo agora (marco de meia-noite ainda no futuro, D11). */
    val pausedToday: Flow<Boolean> =
        dataStore.data.map { (it[Keys.PAUSED_UNTIL] ?: 0L) > time.now() }

    /** Grava o limite; rejeita fora de 10–60 sem alterar o valor guardado. */
    suspend fun setLimit(minutes: Int): Result<Unit> {
        if (minutes !in MIN_LIMIT..MAX_LIMIT) {
            return Result.failure(IllegalArgumentException("limite fora de $MIN_LIMIT–$MAX_LIMIT: $minutes"))
        }
        withContext(io) { dataStore.edit { it[Keys.LIMIT] = minutes } }
        return Result.success(Unit)
    }

    /** Ativa "pausar por hoje" até a próxima meia-noite (só suprime avisos, não zera a contagem). */
    suspend fun pauseForToday() = withContext(io) {
        dataStore.edit { it[Keys.PAUSED_UNTIL] = midnight.nextMidnight(time.now()) }
    }

    companion object {
        const val DEFAULT_LIMIT = 20
        const val MIN_LIMIT = 10
        const val MAX_LIMIT = 60
    }
}
