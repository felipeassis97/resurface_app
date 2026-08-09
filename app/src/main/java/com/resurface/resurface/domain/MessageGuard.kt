package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Message

/**
 * Filtra a saída GERADA (D-5): rejeita cobrança (P5) e afirmação de estado mental (P2), além de
 * tamanho absurdo. Puro. Os templates à mão já são seguros — o guard protege o texto do LLM.
 */
class MessageGuard {

    /** Verdadeiro se a mensagem é segura pra mostrar (não fere P2/P5 nem estoura o tamanho). */
    fun isSafe(message: Message): Boolean {
        val text = (message.title + " " + message.body).lowercase()
        if (FORBIDDEN.any { text.contains(it) }) return false
        if (message.title.length > MAX_TITLE || message.body.length > MAX_BODY) return false
        return true
    }

    private companion object {
        // P5 (cobrança/culpa) e P2 (estado mental) — padrões proibidos.
        val FORBIDDEN = listOf(
            "devia", "deveria", "larga o", "larga esse", "precisa parar", "para de",
            "no automático", "vidrad", "viciad", "sem perceber", "você falhou",
            "perdeu tempo", "desperdi", "que vergonha",
        )
        const val MAX_TITLE = 50
        const val MAX_BODY = 120
    }
}
