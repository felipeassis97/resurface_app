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

    /** Prompt restrito: uma simulação fiel do que um modelo pequeno on-device daria (comentário PT, prompt EN). */
    private fun buildPrompt(profile: Profile, moment: Moment): String {
        val tone = when (profile.tone) {
            Tone.DIRETO -> "direct and factual"
            Tone.GENTIL -> "gentle and warm"
            Tone.BEM_HUMORADO -> "playful (aim at the algorithm/feed, never the person)"
        }
        val nameClause = if (profile.name.isNotBlank()) " Their name is ${profile.name} (you may address them by it)." else ""
        val hobby = profile.anyHobby()
        val hobbyClause = if (hobby != null) " They enjoy $hobby (mention it lightly, as an invitation, never an obligation)." else ""
        return """
            Write ONE short screen-time reminder, in English, in a $tone tone.$nameClause
            They have been on ${moment.appLabel} for ${moment.minutes} minutes, at ${moment.hour}h.$hobbyClause
            Strict rules:
            - Do NOT say what they should be doing.
            - Do NOT use dashes in the text.
            - Do NOT assert their mental state (no "on autopilot", "glued", "addicted").
            - Do NOT blame or shame.
            Reply in TWO short lines, plain human text, no quotes and no explanation:
            line 1 = the main sentence (max 8 words)
            line 2 = a light question or remark (max 10 words)
        """.trimIndent()
    }
}
