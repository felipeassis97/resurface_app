package com.resurface.resurface.data.usage

import com.resurface.resurface.domain.model.UsageEvent

/**
 * Fonte de eventos de primeiro plano dos apps-alvo (G5). O domínio depende desta abstração,
 * não do `UsageStatsManager` — o que permite fake nos testes.
 */
interface UsageStatsReader {

    /** Eventos de domínio dos alvos na janela [from, to], ordenados por tempo. Fora da main. */
    suspend fun events(from: Long, to: Long): List<UsageEvent>

    /** Se a permissão de acesso ao uso está concedida agora (lida ao vivo do OS, G3). */
    fun hasUsageAccess(): Boolean
}
