package com.resurface.resurface.data.outcome

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Resposta subjetiva do usuário a um aviso (F7). */
enum class AlertResponse(val stored: String) {
    ERA_HORA("era_hora"),
    AGORA_NAO("agora_nao"),
}

/**
 * Um aviso disparado e a resposta (se houve). `response`/`respondedAt` nulos = ainda sem
 * resposta (ignorar é válido, P3). O instrumento que mede o H1 e a S2.
 */
@Entity(tableName = "alert_outcome")
data class AlertOutcomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firedAt: Long,
    val appLabel: String,
    val response: String? = null,
    val respondedAt: Long? = null,
    /** Tom da mensagem mostrada (H4). */
    val tone: String? = null,
    /** Fonte do texto: "generated" (Gemini) ou "template" (à mão). */
    val source: String? = null,
)
