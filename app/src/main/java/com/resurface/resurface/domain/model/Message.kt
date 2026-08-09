package com.resurface.resurface.domain.model

/** O momento do aviso: o que a mensagem pode referenciar (só o que se mede, P2). */
data class Moment(val minutes: Int, val appLabel: String, val hour: Int)

/** O texto do aviso: título (heads-up) + corpo. */
data class Message(val title: String, val body: String)

/** Origem do texto usado no aviso (pra análise por tom, H4). */
enum class MessageSource { GENERATED, TEMPLATE }
