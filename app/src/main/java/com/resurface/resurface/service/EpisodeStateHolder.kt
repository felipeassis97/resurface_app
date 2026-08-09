package com.resurface.resurface.service

import com.resurface.resurface.domain.model.EpisodeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Fonte única do estado do episódio ao vivo (Q-A opção 1): o serviço escreve, a UI lê. */
@Singleton
class EpisodeStateHolder @Inject constructor() {

    private val _state = MutableStateFlow(EpisodeState.INITIAL)

    /** Estado corrente observável. Tolerante a estar frio (o receiver usa replay, não isto). */
    val state: StateFlow<EpisodeState> = _state.asStateFlow()

    /** Atualiza o estado corrente (chamado pelo serviço a cada tick). */
    fun set(newState: EpisodeState) {
        _state.value = newState
    }
}
