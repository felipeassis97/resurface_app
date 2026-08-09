package com.resurface.resurface.data.config

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.resurface.resurface.di.IoDispatcher
import com.resurface.resurface.domain.model.Config
import com.resurface.resurface.domain.model.Schedule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
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
        val SCHEDULE = stringPreferencesKey("active_schedule")
    }

    /** Limite de minutos gravado, com padrão. */
    val limitMinutes: Flow<Int> = dataStore.data.map { it[Keys.LIMIT] ?: DEFAULT_LIMIT }

    /** Janela ativa gravada; vazia (sempre ativo) se nada foi configurado. */
    val schedule: Flow<Schedule> = dataStore.data.map { decodeSchedule(it[Keys.SCHEDULE]) }

    /** O Config de domínio montado a partir do que está gravado (limite + janela). */
    val config: Flow<Config> = combine(limitMinutes, schedule) { limit, sched ->
        Config(limitMinutes = limit, schedule = sched)
    }

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

    /** Grava a janela ativa (allow-list) na forma compacta "DIAS|startMin|endMin". */
    suspend fun setSchedule(schedule: Schedule) = withContext(io) {
        dataStore.edit { it[Keys.SCHEDULE] = encodeSchedule(schedule) }
    }

    /** Serializa a janela: "MON,TUE|1080|1380"; dias vazios → "|start|end". */
    private fun encodeSchedule(s: Schedule): String {
        val days = s.days.joinToString(",") { it.name }
        return "$days|${s.startMinute}|${s.endMinute}"
    }

    /** Desserializa a janela; qualquer formato inválido/nulo vira janela vazia (sempre ativo). */
    private fun decodeSchedule(raw: String?): Schedule {
        if (raw.isNullOrBlank()) return Schedule()
        val parts = raw.split("|")
        if (parts.size != 3) return Schedule()
        val days = parts[0].split(",").filter { it.isNotBlank() }
            .mapNotNull { runCatching { DayOfWeek.valueOf(it) }.getOrNull() }.toSet()
        val start = parts[1].toIntOrNull() ?: Schedule.DEFAULT_START
        val end = parts[2].toIntOrNull() ?: Schedule.DEFAULT_END
        return Schedule(days = days, startMinute = start, endMinute = end)
    }

    companion object {
        const val DEFAULT_LIMIT = 20
        const val MIN_LIMIT = 10
        const val MAX_LIMIT = 60
    }
}
