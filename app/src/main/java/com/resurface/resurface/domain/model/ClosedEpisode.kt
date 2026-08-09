package com.resurface.resurface.domain.model

/**
 * Um episódio fechado e imutável — o que vira linha no histórico (Room, camada de dados).
 * Emitido pelo EpisodeEngine quando o episódio termina (5 min fora de qualquer alvo).
 */
data class ClosedEpisode(
    val startedAt: Long,
    val endedAt: Long,
    val accumulatedMs: Long,
    val apps: Set<String>,
)
