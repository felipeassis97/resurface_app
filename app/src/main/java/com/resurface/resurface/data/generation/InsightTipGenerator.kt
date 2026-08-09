package com.resurface.resurface.data.generation

import com.resurface.resurface.domain.Insight
import com.resurface.resurface.domain.MessageGuard
import com.resurface.resurface.domain.model.Message
import com.resurface.resurface.domain.model.Tone
import javax.inject.Inject

/**
 * Reescreve o fato do tip em 2 linhas no tom, via Gemini (proxy do Nano). Só o fato-resumo + tom
 * vão no prompt (nunca os dados crus, P4). Passa a saída pelo [MessageGuard]; null em qualquer
 * falha ou rejeição → o chamador mantém a frase local.
 */
class InsightTipGenerator @Inject constructor(
    private val client: GeminiClient,
) {
    private val guard = MessageGuard()

    suspend fun generate(insight: Insight, tone: Tone): Message? {
        val raw = client.generate(buildPrompt(insight, tone), maxTokens = 60) ?: return null
        val lines = raw.lines().map { it.trim().removeSurrounding("\"") }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return null
        val message = Message(title = lines[0], body = lines.getOrElse(1) { "" })
        return if (guard.isSafe(message)) message else null
    }

    private fun buildPrompt(insight: Insight, tone: Tone): String {
        val toneDesc = when (tone) {
            Tone.DIRETO -> "direct and factual"
            Tone.GENTIL -> "gentle and warm"
            Tone.BEM_HUMORADO -> "playful (aim at the algorithm/feed, never the person)"
        }
        return """
            Rephrase this short-video usage fact as a friendly tip, in English, in a $toneDesc tone.
            Fact: ${insight.fact}
            Strict rules:
            - Keep the number/fact exactly as given, do not invent new stats.
            - Do NOT say what they should be doing.
            - Do NOT use dashes in the text.
            - Do NOT blame or shame, and do NOT assert their mental state.
            Reply in TWO short lines, plain human text, no quotes and no explanation:
            line 1 = the main sentence (max 8 words)
            line 2 = a light remark (max 10 words)
        """.trimIndent()
    }
}
