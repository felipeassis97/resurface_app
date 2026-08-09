package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Config
import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.EpisodeState

/**
 * Calcula QUANDO o alarme deve disparar (D-2), puro e testável. O serviço só agenda o resultado.
 * Espelha a AlertPolicy: mesmo limite (`limite × 2^avisos`), mesmas guardas (pausa, teto, DENTRO).
 */
class AlarmPlanner {

    /**
     * Delay em ms até o acumulado cruzar o limite atual, ou null se não deve agendar
     * (fora de DENTRO, pausado por hoje, ou teto diário batido). 0 = já cruzou, dispara agora.
     */
    fun nextFireDelayMs(
        state: EpisodeState,
        config: Config,
        alertsFired: Int,
        pausedToday: Boolean,
        todayAlertCount: Int,
        now: Long,
        isActiveNow: Boolean = true,
        nextOpeningDelayMs: Long? = null,
    ): Long? {
        if (state.phase != EpisodePhase.DENTRO) return null
        if (pausedToday) return null
        if (todayAlertCount >= AlertPolicy.MAX_ALERTS_PER_DAY) return null
        // Fora da janela: acorda na abertura dela (ou não agenda se não há abertura futura).
        if (!isActiveNow) return nextOpeningDelayMs
        val thresholdMs = (config.limitMinutes shl alertsFired) * 60_000L
        val remaining = thresholdMs - state.accumulatedMsAt(now)
        return remaining.coerceAtLeast(0L)
    }
}
