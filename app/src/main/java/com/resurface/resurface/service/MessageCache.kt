package com.resurface.resurface.service

import com.resurface.resurface.domain.model.Message
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache das mensagens pré-geradas, por (início do episódio, limite em minutos). Desacopla a rede
 * do instante do disparo (D-6): o tick pré-gera e guarda; o alarme lê. Em memória (perde no restart,
 * ok — o fallback à mão cobre).
 */
@Singleton
class MessageCache @Inject constructor() {

    private val map = ConcurrentHashMap<Key, Message>()

    /** Mensagem pré-gerada pra esse episódio/limite, ou null. */
    fun get(episodeStartedAt: Long, thresholdMinutes: Int): Message? =
        map[Key(episodeStartedAt, thresholdMinutes)]

    /** Guarda a mensagem pré-gerada. */
    fun put(episodeStartedAt: Long, thresholdMinutes: Int, message: Message) {
        map[Key(episodeStartedAt, thresholdMinutes)] = message
    }

    /** Verdadeiro se já há mensagem pra esse episódio/limite (evita re-gerar a cada tick). */
    fun has(episodeStartedAt: Long, thresholdMinutes: Int): Boolean =
        map.containsKey(Key(episodeStartedAt, thresholdMinutes))

    private data class Key(val episodeStartedAt: Long, val thresholdMinutes: Int)
}
