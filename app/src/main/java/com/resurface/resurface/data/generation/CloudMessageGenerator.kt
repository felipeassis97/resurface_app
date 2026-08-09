package com.resurface.resurface.data.generation

import com.resurface.resurface.domain.MessageGenerator
import com.resurface.resurface.domain.model.Message
import com.resurface.resurface.domain.model.Moment
import com.resurface.resurface.domain.model.Profile
import com.resurface.resurface.domain.model.Tone
import javax.inject.Inject

/**
 * Impl do [MessageGenerator] via Gemini cloud (proxy do Nano). Monta o prompt restrito (tom +
 * momento + regras P2/P5), pede duas linhas curtas, e monta o [Message]. Null em qualquer falha.
 */
class CloudMessageGenerator @Inject constructor(
    private val client: GeminiClient,
) : MessageGenerator {

    /** Gera pelo cloud; devolve null se o client falhar ou vier vazio (→ fallback à mão). */
    override suspend fun generate(profile: Profile, moment: Moment): Message? {
        val raw = client.generate(buildPrompt(profile, moment), maxTokens = 60) ?: return null
        val lines = raw.lines().map { it.trim().removeSurrounding("\"") }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return null
        return Message(title = lines[0], body = lines.getOrElse(1) { "" })
    }

    /** Prompt restrito: uma simulação fiel do que um modelo pequeno on-device daria. */
    private fun buildPrompt(profile: Profile, moment: Moment): String {
        val tom = when (profile.tone) {
            Tone.DIRETO -> "direto e factual"
            Tone.GENTIL -> "gentil e acolhedor"
            Tone.BEM_HUMORADO -> "bem-humorado (mire o algoritmo/feed, nunca a pessoa)"
        }
        val hobby = profile.anyHobby()
        val hobbyClause = if (hobby != null) " A pessoa gosta de $hobby (mencione de leve, como convite, nunca obrigação)." else ""
        return """
            Você escreve UM lembrete curto de tempo de tela, em português do Brasil, no tom $tom.
            A pessoa está há ${moment.minutes} minutos no ${moment.appLabel}, às ${moment.hour}h.$hobbyClause
            Regras rígidas:
            - NÃO diga o que ela deveria estar fazendo.
            - NÃO USE - nos textos.
            - NÃO afirme o estado mental dela (nada de "no automático", "vidrado", "viciado").
            - NÃO culpe nem envergonhe.
            Responda em DUAS linhas curtas, em texto humanizado sem aspas e sem explicação:
            linha 1 = a frase principal (máx 8 palavras)
            linha 2 = uma pergunta ou observação leve (máx 10 palavras)
        """.trimIndent()
    }
}
