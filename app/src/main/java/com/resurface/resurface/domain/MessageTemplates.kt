package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Tone

/** Um modelo de mensagem à mão, com slots `{min}` `{app}` `{hobby}` `{name}`. */
data class MessageTemplate(
    val title: String,
    val body: String,
    val usesHobby: Boolean = false,
    val usesName: Boolean = false,
)

/**
 * Pools de mensagens à mão por tom (D8), em inglês, a fundação e o fallback. Curadas pra respeitar
 * P2 (só afirma o que mede, nada de estado mental) e P5 (sem cobrança/culpa; o hobby é convite). As
 * piadas do tom bem-humorado miram o algoritmo/feed, nunca a pessoa. Sem travessões (soa humano).
 */
object MessageTemplates {

    val pools: Map<Tone, List<MessageTemplate>> = mapOf(
        Tone.DIRETO to listOf(
            MessageTemplate("{min} minutes on {app}.", "Still what you want to be doing?"),
            MessageTemplate("{min} min here.", "Just so you know."),
            MessageTemplate("{app}: {min} minutes.", "Carry on if you like."),
            MessageTemplate("{min} minutes in.", "No pressure, just the number."),
            MessageTemplate("{min} min of short video.", "You pick the next one."),
            MessageTemplate("{name}, {min} min on {app}.", "Your call.", usesName = true),
            MessageTemplate("{min} min on {app}.", "{hobby} is there too, if you feel like it.", usesHobby = true),
        ),
        Tone.GENTIL to listOf(
            MessageTemplate("It has been {min} minutes.", "How are you right now?"),
            MessageTemplate("{min} min on {app}.", "No rush, just saying hi."),
            MessageTemplate("A little while passed ({min} min).", "How about a breath?"),
            MessageTemplate("{min} minutes, gently noted.", "You are in charge."),
            MessageTemplate("Hey {name}, {min} min here.", "All good?", usesName = true),
            MessageTemplate("{min} min here.", "{hobby} will be there whenever you want.", usesHobby = true),
            MessageTemplate("Hey {name}.", "{min} min in. A bit of {hobby} sounds nice too.", usesHobby = true, usesName = true),
        ),
        Tone.BEM_HUMORADO to listOf(
            MessageTemplate("Score: algorithm {min}, you 0.", "Rematch?"),
            MessageTemplate("{min} min on {app}.", "The feed never ends. It literally never ends."),
            MessageTemplate("News flash: {min} minutes.", "Time snuck out the back."),
            MessageTemplate("{min} min of scrolling.", "Your thumb deserves a break."),
            MessageTemplate("Rabbit hole alert: {min} min.", "It happens."),
            MessageTemplate("{name}, {min} minutes.", "See you on the other side?", usesName = true),
            MessageTemplate("{min} min here.", "{hobby} called, it misses you.", usesHobby = true),
        ),
    )
}
