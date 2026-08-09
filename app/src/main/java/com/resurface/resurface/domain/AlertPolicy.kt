package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.AlertDecision
import com.resurface.resurface.domain.model.Config

/**
 * Política de disparo do aviso (D4/D18/D5/D11), pura e sem estado (D-2).
 * O "próximo limite" é derivado de quantos avisos já saíram no episódio — nada persistido (D24).
 */
class AlertPolicy {

    /**
     * Decide se avisar agora. Segura se pausado no dia (D11) ou se o teto diário foi batido
     * (D5); senão dispara quando o acumulado cruza `limite × 2^avisosDoEpisódio` (D18).
     */
    fun decide(
        accumulatedMs: Long,
        alertsFiredThisEpisode: Int,
        config: Config,
        todayAlertCount: Int,
        pausedToday: Boolean,
        isActiveNow: Boolean = true,
    ): AlertDecision {
        if (pausedToday) return AlertDecision.Hold
        if (!isActiveNow) return AlertDecision.Hold   // fora da janela ativa (allow-list)
        if (todayAlertCount >= MAX_ALERTS_PER_DAY) return AlertDecision.Hold
        val thresholdMinutes = config.limitMinutes shl alertsFiredThisEpisode   // × 2^n
        val thresholdMs = thresholdMinutes * 60_000L
        return if (accumulatedMs >= thresholdMs) AlertDecision.Fire(thresholdMinutes)
        else AlertDecision.Hold
    }

    companion object {
        /** Teto de segurança: no máximo 6 avisos por dia (D5). */
        const val MAX_ALERTS_PER_DAY = 6
    }
}
