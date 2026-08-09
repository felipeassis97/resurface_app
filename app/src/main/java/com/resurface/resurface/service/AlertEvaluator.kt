package com.resurface.resurface.service

import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.config.MidnightClock
import com.resurface.resurface.data.config.TimeProvider
import com.resurface.resurface.data.alarm.AlarmScheduler
import com.resurface.resurface.data.episode.EpisodeRepository
import com.resurface.resurface.data.notification.Notifier
import com.resurface.resurface.data.outcome.OutcomeRepository
import com.resurface.resurface.data.profile.ProfileRepository
import com.resurface.resurface.data.usage.UsageStatsReader
import com.resurface.resurface.di.IoDispatcher
import com.resurface.resurface.domain.AlarmPlanner
import com.resurface.resurface.domain.AlertPolicy
import com.resurface.resurface.domain.AppLabels
import com.resurface.resurface.domain.EpisodeEngine
import com.resurface.resurface.domain.MessageGenerator
import com.resurface.resurface.domain.MessageGuard
import com.resurface.resurface.domain.TemplateComposer
import com.resurface.resurface.domain.model.AlertDecision
import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.EpisodeState
import com.resurface.resurface.domain.model.Message
import com.resurface.resurface.domain.model.MessageSource
import com.resurface.resurface.domain.model.Moment
import com.resurface.resurface.domain.model.Profile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * O cérebro fino que junta as camadas (D-1/D-3). Reusado pelo tick ([refresh]) e pelo disparo
 * ([onAlarmFired]). Estado vem do replay do UsageStats (D24). O texto do aviso é composto no tom:
 * gerado (Gemini, pré-gerado+cache) → guard P2/P5 → fallback à mão. A IA nunca decide SE avisa (D9).
 */
@Singleton
class AlertEvaluator @Inject constructor(
    private val reader: UsageStatsReader,
    private val configRepo: ConfigRepository,
    private val profileRepo: ProfileRepository,
    private val episodes: EpisodeRepository,
    private val outcomes: OutcomeRepository,
    private val notifier: Notifier,
    private val scheduler: AlarmScheduler,
    private val holder: EpisodeStateHolder,
    private val generator: MessageGenerator,
    private val cache: MessageCache,
    private val time: TimeProvider,
    private val midnight: MidnightClock,
    @IoDispatcher io: CoroutineDispatcher,
) {
    private val engine = EpisodeEngine()
    private val policy = AlertPolicy()
    private val planner = AlarmPlanner()
    private val templates = TemplateComposer()
    private val guard = MessageGuard()
    private val genScope = CoroutineScope(io + SupervisorJob())

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
                postComposed(state, decision.limitMinutes, alertsFired, now)
                firedNow = true
            }
        }
        // Se acabou de postar, o próximo limite já dobrou — conta +1 mesmo que a notificação
        // própria ainda não tenha aparecido no UsageStats (evita re-disparo imediato).
        reschedule(state, now, extraFired = if (firedNow) 1 else 0)
    }

    /** Compõe o texto (cache gerado, ou template à mão), posta e registra o outcome com tom+fonte. */
    private suspend fun postComposed(state: EpisodeState, thresholdMin: Int, seed: Int, now: Long) {
        val profile = profileRepo.profile.first()
        val label = AppLabels.of(state.currentApp)
        val cached = cache.get(state.episodeStartedAt, thresholdMin)
        val (message, source) = if (cached != null) {
            cached to MessageSource.GENERATED
        } else {
            templates.compose(profile, Moment(thresholdMin, label, hourOf(now)), seed) to MessageSource.TEMPLATE
        }
        val id = outcomes.recordFired(now, label, profile.tone.name, source.name.lowercase())
        notifier.postAlert(message.title, message.body, id)
    }

    /** Replay da janela recente → estado; arquiva fechados (idempotente) e publica no holder. */
    private suspend fun rebuild(now: Long): EpisodeState {
        val (state, closed) = engine.run(reader.events(now - WINDOW_MS, now))
        closed.forEach { episodes.archive(it) }
        holder.set(state)
        return state
    }

    /** Recalcula e reagenda (ou cancela) o alarme; pré-gera a mensagem do próximo limite (D-6). */
    private suspend fun reschedule(state: EpisodeState, now: Long, extraFired: Int) {
        val cfg = configRepo.config.first()
        val paused = configRepo.pausedToday.first()
        val fired = alertsFiredInEpisode(state, now) + extraFired
        val today = alertsToday(now) + extraFired
        val delay = planner.nextFireDelayMs(state, cfg, fired, paused, today, now)
        if (delay == null) {
            scheduler.cancel()
        } else {
            scheduler.scheduleInMs(delay)
            preGenerate(state, cfg.limitMinutes shl fired, now)
        }
    }

    /** Gera async a mensagem do próximo limite e cacheia (uma vez por limite). Falha → fica sem cache. */
    private fun preGenerate(state: EpisodeState, thresholdMin: Int, now: Long) {
        if (cache.has(state.episodeStartedAt, thresholdMin)) return
        genScope.launch {
            val profile = profileRepo.profile.first()
            val moment = Moment(thresholdMin, AppLabels.of(state.currentApp), hourOf(now))
            val msg: Message? = generator.generate(profile, moment)
            if (msg != null && guard.isSafe(msg)) cache.put(state.episodeStartedAt, thresholdMin, msg)
        }
    }

    /** Avisos já disparados no episódio corrente, contados da nossa tabela de outcome (F7). */
    private suspend fun alertsFiredInEpisode(state: EpisodeState, now: Long): Int =
        if (state.phase == EpisodePhase.DENTRO) outcomes.countInEpisode(state.episodeStartedAt, now) else 0

    /** Total de avisos hoje (desde a meia-noite), pro teto diário (D5). */
    private suspend fun alertsToday(now: Long): Int =
        outcomes.countSince(midnight.startOfDay(now))

    /** Hora do dia (0–23) do instante, no fuso do dispositivo. */
    private fun hourOf(now: Long): Int = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).hour

    private companion object {
        const val WINDOW_MS = 6L * 60 * 60 * 1000   // 6 h de replay (D24)
    }
}
