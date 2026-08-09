package com.resurface.resurface.domain.model

/**
 * Vocabulário de entrada do [com.resurface.resurface.domain.EpisodeEngine] (PRODUTO §5.3).
 * Kotlin puro — a tradução do `UsageEvents.Event` cru pra cá é da camada de dados (G1/G5).
 */
sealed interface UsageEvent {
    /** Instante do evento, em epoch millis. */
    val timestamp: Long

    /** Entrou em primeiro plano num app-alvo (ACTIVITY_RESUMED). */
    data class Enter(val pkg: String, override val timestamp: Long) : UsageEvent

    /** Saiu do primeiro plano de um app-alvo (ACTIVITY_PAUSED). */
    data class Leave(val pkg: String, override val timestamp: Long) : UsageEvent

    /** A tela apagou (SCREEN_NON_INTERACTIVE) — pausa como se tivesse saído. */
    data class ScreenOff(override val timestamp: Long) : UsageEvent
}
