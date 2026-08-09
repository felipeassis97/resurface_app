package com.resurface.resurface.domain

import com.resurface.resurface.domain.model.ClosedEpisode
import com.resurface.resurface.domain.model.Config
import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.EpisodeState
import com.resurface.resurface.domain.model.UsageEvent

/** Resultado de um passo do motor: novo estado + episódio fechado, se houve. */
data class EpisodeStep(val state: EpisodeState, val closed: ClosedEpisode? = null)

/**
 * Máquina de estados do episódio (PRODUTO §5.3), pura e determinística (D24, D-1).
 * É um redutor sem estado interno: o chamador guarda o [EpisodeState]. Reusado no tick ao
 * vivo, no replay do cold-start e na releitura do alarme.
 */
class EpisodeEngine(private val config: Config = Config()) {

    /** Aplica um evento ao estado; fecha o episódio se a janela estourou antes do evento. */
    fun reduce(state: EpisodeState, event: UsageEvent): EpisodeStep {
        val (base, closedByTimeout) = closeIfExpired(state, event.timestamp)
        val next = when (event) {
            is UsageEvent.Enter -> onEnter(base, event)
            is UsageEvent.Leave -> onLeave(base, event)
            is UsageEvent.ScreenOff -> onPause(base, event.timestamp)
        }
        return if (closedByTimeout != null) next.copy(closed = closedByTimeout) else next
    }

    /** Avalia só a passagem do tempo: fecha um PAUSADO que estourou a janela de retorno. */
    fun tick(state: EpisodeState, now: Long): EpisodeStep {
        val (base, closed) = closeIfExpired(state, now)
        return EpisodeStep(base, closed)
    }

    /** Roda o stream inteiro pelo redutor; devolve o estado final e os episódios fechados. */
    fun run(events: List<UsageEvent>): Pair<EpisodeState, List<ClosedEpisode>> {
        var state = EpisodeState.INITIAL
        val closed = mutableListOf<ClosedEpisode>()
        for (e in events) {
            val step = reduce(state, e)
            state = step.state
            step.closed?.let(closed::add)
        }
        return state to closed
    }

    /** Fecha o episódio se está PAUSADO além da janela; senão devolve o estado intacto. */
    private fun closeIfExpired(state: EpisodeState, now: Long): Pair<EpisodeState, ClosedEpisode?> {
        val expired = state.phase == EpisodePhase.PAUSADO &&
            now - state.pausedAt >= config.returnWindowMs
        if (!expired) return state to null
        val closed = ClosedEpisode(
            startedAt = state.episodeStartedAt,
            endedAt = state.pausedAt,          // fim = quando o usuário saiu, não agora
            accumulatedMs = state.bankedMs,
            apps = state.appsInEpisode,
        )
        return EpisodeState.INITIAL to closed
    }

    /** Entrada num alvo: abre episódio (FORA), retoma (PAUSADO) ou troca de app (DENTRO). */
    private fun onEnter(state: EpisodeState, e: UsageEvent.Enter): EpisodeStep {
        if (!config.isTarget(e.pkg)) return EpisodeStep(state)
        return when (state.phase) {
            EpisodePhase.FORA -> EpisodeStep(startEpisode(e.pkg, e.timestamp))
            EpisodePhase.PAUSADO -> EpisodeStep(
                state.copy(
                    phase = EpisodePhase.DENTRO,
                    currentApp = e.pkg,
                    runningSince = e.timestamp,          // o gap pausado não conta
                    appsInEpisode = state.appsInEpisode + e.pkg,
                )
            )
            EpisodePhase.DENTRO ->
                if (e.pkg == state.currentApp) EpisodeStep(state)   // mesma tela, no-op
                else EpisodeStep(                                    // troca contínua (D2)
                    bankRunning(state, e.timestamp).copy(
                        currentApp = e.pkg,
                        runningSince = e.timestamp,
                        appsInEpisode = state.appsInEpisode + e.pkg,
                    )
                )
        }
    }

    /** Saída do app corrente: banca o trecho e vai pra PAUSADO. Saída de outro app é ignorada. */
    private fun onLeave(state: EpisodeState, e: UsageEvent.Leave): EpisodeStep =
        if (state.phase == EpisodePhase.DENTRO && e.pkg == state.currentApp)
            onPause(state, e.timestamp)
        else EpisodeStep(state)

    /** Pausa o episódio DENTRO (saída ou tela apagada): banca o trecho, marca pausedAt. */
    private fun onPause(state: EpisodeState, now: Long): EpisodeStep =
        if (state.phase != EpisodePhase.DENTRO) EpisodeStep(state)
        else EpisodeStep(
            bankRunning(state, now).copy(phase = EpisodePhase.PAUSADO, pausedAt = now)
        )

    /** Soma o trecho DENTRO corrente ao acumulado banked (até [now]). */
    private fun bankRunning(state: EpisodeState, now: Long): EpisodeState =
        state.copy(bankedMs = state.bankedMs + (now - state.runningSince))

    /** Cria um episódio novo em DENTRO a partir de FORA. */
    private fun startEpisode(pkg: String, now: Long): EpisodeState =
        EpisodeState(
            phase = EpisodePhase.DENTRO,
            bankedMs = 0L,
            currentApp = pkg,
            episodeStartedAt = now,
            runningSince = now,
            pausedAt = 0L,
            appsInEpisode = setOf(pkg),
        )
}
