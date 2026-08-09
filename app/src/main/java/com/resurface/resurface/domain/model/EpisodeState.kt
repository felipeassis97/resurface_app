package com.resurface.resurface.domain.model

/** Fase do episódio na máquina de estados (PRODUTO §5.3). */
enum class EpisodePhase { FORA, DENTRO, PAUSADO }

/**
 * Estado imutável do episódio corrente. Derivável só do stream de eventos + relógio (D24),
 * sem nada escondido — por isso o replay é determinístico.
 *
 * `bankedMs` = tempo já contado de trechos DENTRO fechados neste episódio.
 * `runningSince` = início do trecho DENTRO corrente (válido em DENTRO).
 * `pausedAt` = quando pausou (válido em PAUSADO, mede a janela de retorno).
 */
data class EpisodeState(
    val phase: EpisodePhase,
    val bankedMs: Long,
    val currentApp: String?,
    val episodeStartedAt: Long,
    val runningSince: Long,
    val pausedAt: Long,
    val appsInEpisode: Set<String>,
) {
    /** Acumulado vivo no instante [now]: banked + trecho corrente se DENTRO; senão só banked. */
    fun accumulatedMsAt(now: Long): Long =
        if (phase == EpisodePhase.DENTRO) bankedMs + (now - runningSince) else bankedMs

    companion object {
        /** Estado de partida: fora de qualquer app-alvo, acumulado zero. */
        val INITIAL = EpisodeState(EpisodePhase.FORA, 0L, null, 0L, 0L, 0L, emptySet())
    }
}
