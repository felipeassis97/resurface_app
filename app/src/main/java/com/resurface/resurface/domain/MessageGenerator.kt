package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Message
import com.resurface.resurface.domain.model.Moment
import com.resurface.resurface.domain.model.Profile

/**
 * Gera a mensagem do aviso no tom (G5). É o SEAM Nano-ready: hoje `CloudMessageGenerator` (Gemini
 * Flash, proxy do Nano); no futuro `NanoMessageGenerator` no hardware — trocar é um binding.
 * Devolve null em qualquer falha (→ fallback à mão).
 */
interface MessageGenerator {
    /** Gera a mensagem pro perfil + momento, ou null se não deu (rede, erro, sem chave, vazio). */
    suspend fun generate(profile: Profile, moment: Moment): Message?
}
