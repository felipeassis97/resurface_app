package com.resurface.resurface.data.usage

import com.resurface.resurface.domain.model.UsageEvent

/**
 * Traduz um evento cru do UsageStats pro modelo de domínio, descartando ruído na fronteira.
 * Puro de propósito: trabalha com o código do tipo (Int), sem importar `android.*`, pra o
 * teste rodar em JVM e o domínio nunca ver Android (G1/G5, D-1).
 */
object UsageEventMapper {

    // Valores oficiais e estáveis de android.app.usage.UsageEvents.Event.
    private const val ACTIVITY_RESUMED = 1
    private const val ACTIVITY_PAUSED = 2
    private const val SCREEN_NON_INTERACTIVE = 16

    /** Mapeia (tipo, pacote, timestamp) pra um UsageEvent, ou null se for ruído/não-alvo. */
    fun map(type: Int, pkg: String, timestamp: Long, targets: Set<String>): UsageEvent? =
        when (type) {
            ACTIVITY_RESUMED -> if (pkg in targets) UsageEvent.Enter(pkg, timestamp) else null
            ACTIVITY_PAUSED -> if (pkg in targets) UsageEvent.Leave(pkg, timestamp) else null
            SCREEN_NON_INTERACTIVE -> UsageEvent.ScreenOff(timestamp)
            else -> null
        }
}
