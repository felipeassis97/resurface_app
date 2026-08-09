package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.Tone

/** Um modelo de mensagem à mão, com slots `{min}` `{app}` `{hobby}`. */
data class MessageTemplate(val title: String, val body: String, val usesHobby: Boolean = false)

/**
 * Pools de mensagens à mão por tom (D8) — a fundação e o fallback. Curadas pra respeitar P2 (só
 * afirma o que mede, nada de estado mental) e P5 (sem cobrança/culpa; o hobby é convite, nunca
 * "você devia"). As piadas do tom bem-humorado miram o algoritmo/feed, não a pessoa.
 */
object MessageTemplates {

    val pools: Map<Tone, List<MessageTemplate>> = mapOf(
        Tone.DIRETO to listOf(
            MessageTemplate("{min} minutos no {app}.", "Ainda é isso que você quer estar fazendo?"),
            MessageTemplate("{min} min por aqui.", "Só pra você saber."),
            MessageTemplate("{app}: {min} minutos.", "Segue se quiser."),
            MessageTemplate("Você está há {min} min no {app}.", "Decisão sua."),
            MessageTemplate("Passaram {min} minutos.", "Nenhuma cobrança — só o número."),
            MessageTemplate("{min} min de vídeo curto.", "Você decide o próximo."),
            MessageTemplate("{min} minutos aqui.", "Fim natural não existe nessa tela. Este é um."),
            MessageTemplate("{min} min no {app}.", "{hobby} também está aí, se der vontade.", usesHobby = true),
        ),
        Tone.GENTIL to listOf(
            MessageTemplate("Ei — {min} min por aqui.", "Tudo bem por aí?"),
            MessageTemplate("Já faz {min} minutos.", "Como você está agora?"),
            MessageTemplate("{min} min no {app}.", "Sem pressa — só um oi."),
            MessageTemplate("Um tempinho passou ({min} min).", "Que tal um respiro?"),
            MessageTemplate("{min} minutos, lembrando com carinho.", "Você no comando."),
            MessageTemplate("Oi — {min} min no {app}.", "Só passando pra dizer olá."),
            MessageTemplate("{min} minutos.", "Está tudo certo? Fique à vontade."),
            MessageTemplate("{min} min por aqui.", "{hobby} espera por você quando quiser.", usesHobby = true),
            MessageTemplate("{min} min já.", "Um pouco de {hobby} também faz bem.", usesHobby = true),
        ),
        Tone.BEM_HUMORADO to listOf(
            MessageTemplate("Placar: algoritmo {min}, você 0.", "Empate técnico?"),
            MessageTemplate("{min} min no {app}.", "O feed não vai acabar. Ele nunca acaba. 🌀"),
            MessageTemplate("Notícia: já são {min} minutos.", "O tempo fugiu de fininho."),
            MessageTemplate("{min} min de scroll.", "Seu polegar merece férias."),
            MessageTemplate("{app}, {min} minutos.", "A gente se vê do outro lado?"),
            MessageTemplate("{min} minutos depois…", "…e o vídeo ainda está bom, né?"),
            MessageTemplate("Alerta de buraco de coelho: {min} min.", "Tudo bem, acontece."),
            MessageTemplate("{min} min aqui.", "{hobby} ligou, está com saudade.", usesHobby = true),
            MessageTemplate("{min} min de {app}.", "{hobby} também é rolê, só lembrando 😄", usesHobby = true),
        ),
    )
}
