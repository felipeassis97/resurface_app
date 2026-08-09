package com.resurface.resurface.service

import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.config.MidnightClock
import com.resurface.resurface.data.config.TimeProvider
import com.resurface.resurface.data.alarm.AlarmScheduler
import com.resurface.resurface.data.episode.EpisodeRepository
import com.resurface.resurface.data.notification.Notifier
import com.resurface.resurface.data.outcome.OutcomeRepository
import com.resurface.resurface.data.usage.UsageStatsReader
import com.resurface.resurface.domain.AlarmPlanner
import com.resurface.resurface.domain.AppLabels
import com.resurface.resurface.domain.AlertPolicy
import com.resurface.resurface.domain.EpisodeEngine
import com.resurface.resurface.domain.model.AlertDecision
import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.EpisodeState
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * O cérebro fino que junta as camadas (D-1/D-3). Reusado pelo tick do serviço ([refresh]) e
 * pelo disparo do alarme ([onAlarmFired]). Todo estado vem do replay do UsageStats (D24) —
 * nada persistido do episódio aberto. As decisões de fato moram no domínio (já testado).
 */
@Singleton
class AlertEvaluator @Inject constructor(
    private val reader: UsageStatsReader,
    private val configRepo: ConfigRepository,
    private val episodes: EpisodeRepository,
    private val outcomes: OutcomeRepository,
    private val notifier: Notifier,
    private val scheduler: AlarmScheduler,
    private val holder: EpisodeStateHolder,
    private val time: TimeProvider,
    private val midnight: MidnightClock,
) {
    private val engine = EpisodeEngine()
    private val policy = AlertPolicy()
    private val planner = AlarmPlanner()

    /** Tick de manutenção: reconstrói o estado, arquiva fechados, atualiza o holder, reagenda. */
    suspend fun refresh() {
        val now = time.now()
        val state = rebuild(now)
        reschedule(state, now, extraFired = 0)
    }

    /** Disparo do alarme: acordar-pra-conferir (D22) — só posta se ainda cabe; senão reagenda. */
    suspend fun onAlarmFired() {
        val now = time.now()
        val state = rebuild(now)
        var firedNow = false
        if (state.phase == EpisodePhase.DENTRO) {
            val cfg = configRepo.config.first()
            val paused = configRepo.pausedToday.first()
            val alertsFired = alertsFiredInEpisode(state, now)
            val today = alertsToday(now)
            val decision = policy.decide(state.accumulatedMsAt(now), alertsFired, cfg, today, paused)
            if (decision is AlertDecision.Fire) {
                val label = AppLabels.of(state.currentApp)
                val id = outcomes.recordFired(now, label)
                notifier.postAlert(label, decision.limitMinutes, id)
                firedNow = true
            }
        }
        // Se acabou de postar, o próximo limite já dobrou — conta +1 mesmo que a notificação
        // própria ainda não tenha aparecido no UsageStats (evita re-disparo imediato).
        reschedule(state, now, extraFired = if (firedNow) 1 else 0)
    }

    /** Replay da janela recente → estado; arquiva fechados (idempotente) e publica no holder. */
    private suspend fun rebuild(now: Long): EpisodeState {
        val (state, closed) = engine.run(reader.events(now - WINDOW_MS, now))
        closed.forEach { episodes.archive(it) }
        holder.set(state)
        return state
    }

    /** Recalcula e reagenda (ou cancela) o alarme do próximo cruzamento. */
    private suspend fun reschedule(state: EpisodeState, now: Long, extraFired: Int) {
        val cfg = configRepo.config.first()
        val paused = configRepo.pausedToday.first()
        val fired = alertsFiredInEpisode(state, now) + extraFired
        val today = alertsToday(now) + extraFired
        val delay = planner.nextFireDelayMs(state, cfg, fired, paused, today, now)
        if (delay == null) scheduler.cancel() else scheduler.scheduleInMs(delay)
    }

    /** Avisos já disparados no episódio corrente, contados da nossa tabela de outcome (F7). */
    private suspend fun alertsFiredInEpisode(state: EpisodeState, now: Long): Int =
        if (state.phase == EpisodePhase.DENTRO) outcomes.countInEpisode(state.episodeStartedAt, now) else 0

    /** Total de avisos hoje (desde a meia-noite), pro teto diário (D5). */
    private suspend fun alertsToday(now: Long): Int =
        outcomes.countSince(midnight.startOfDay(now))

    private companion object {
        const val WINDOW_MS = 6L * 60 * 60 * 1000   // 6 h de replay (D24)
    }
}
