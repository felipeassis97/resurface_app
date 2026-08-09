package com.resurface.resurface.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.data.behavior.BehaviorRepository
import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.config.TimeProvider
import com.resurface.resurface.data.episode.EpisodeRepository
import com.resurface.resurface.data.generation.InsightTipGenerator
import com.resurface.resurface.data.insight.InsightTipRepository
import com.resurface.resurface.data.outcome.OutcomeRepository
import com.resurface.resurface.data.profile.ProfileRepository
import com.resurface.resurface.domain.BehaviorInput
import com.resurface.resurface.domain.InsightSelector
import com.resurface.resurface.domain.InsightTemplates
import com.resurface.resurface.domain.InsightsAggregator
import com.resurface.resurface.domain.InsightsUiState
import com.resurface.resurface.domain.OutcomeInput
import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.EpisodeState
import com.resurface.resurface.domain.model.Message
import com.resurface.resurface.domain.AppLabels
import com.resurface.resurface.service.EpisodeStateHolder
import com.resurface.resurface.ui.tickerFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** Estado ao vivo do episódio corrente (o hero quando ativo). */
data class LiveState(
    val active: Boolean = false,
    val minutes: Int = 0,
    val appLabel: String = "",
    val pausedToday: Boolean = false,
)

/** Mapeia estado do episódio + relógio + pausa pro LiveState. Puro (testável, sem ticker). */
fun toLiveState(state: EpisodeState, nowMillis: Long, paused: Boolean): LiveState =
    LiveState(
        active = state.phase == EpisodePhase.DENTRO,
        minutes = (state.accumulatedMsAt(nowMillis) / 60_000L).toInt(),
        appLabel = state.currentApp?.let { AppLabels.of(it) } ?: "",
        pausedToday = paused,
    )

/** Tudo que o dashboard mostra: nome + tip + estado ao vivo + observações da semana. */
data class DashboardUiState(
    val name: String = "",
    val tip: Message? = null,
    val live: LiveState = LiveState(),
    val insights: InsightsUiState = InsightsUiState(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    holder: EpisodeStateHolder,
    episodes: EpisodeRepository,
    outcomes: OutcomeRepository,
    behavior: BehaviorRepository,
    config: ConfigRepository,
    private val profile: ProfileRepository,
    private val tipRepo: InsightTipRepository,
    private val tipGen: InsightTipGenerator,
    private val time: TimeProvider,
) : ViewModel() {

    private val aggregator = InsightsAggregator()
    private val selector = InsightSelector()
    private val templates = InsightTemplates()
    private val zone: ZoneId = ZoneId.systemDefault()

    private val name = profile.profile.map { it.name }

    private val live = combine(holder.state, config.pausedToday, tickerFlow(1_000)) { state, paused, _ ->
        toLiveState(state, time.now(), paused)
    }

    private val insights: StateFlow<InsightsUiState> =
        combine(episodes.history, outcomes.outcomes, behavior.events) { eps, outs, beh ->
            aggregator.aggregate(
                episodes = eps,
                outcomes = outs.map { OutcomeInput(it.appLabel, it.response, it.firedAt) },
                behavior = beh.map { BehaviorInput(it.timestamp, it.hesitated) },
                nowMillis = time.now(),
                zone = zone,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    /** Índice rotativo: avança 1x por launch. */
    private val rotation = MutableStateFlow(0)

    init {
        viewModelScope.launch { rotation.value = tipRepo.nextRotationIndex() }
    }

    /** Tip: escolhe o fato, mostra a frase local já, e troca pela versão da IA (cache por dia). */
    private val tip: StateFlow<Message?> =
        combine(insights, profile.profile, rotation) { ins, prof, idx ->
            Triple(selector.select(ins, idx), prof.tone, dayKey(time.now()))
        }.flatMapLatest { (insight, tone, day) ->
            flow {
                emit(templates.phrase(insight, tone))
                val factKey = "${insight.fact}|${tone.name}"
                val cached = tipRepo.cachedText(day, factKey)
                if (cached != null) {
                    emit(splitMessage(cached))
                    return@flow
                }
                val ai = tipGen.generate(insight, tone)
                if (ai != null) {
                    tipRepo.cache(day, factKey, "${ai.title}\n${ai.body}")
                    emit(ai)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState: StateFlow<DashboardUiState> =
        combine(name, tip, live, insights) { n, t, l, i -> DashboardUiState(n, t, l, i) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private fun dayKey(now: Long): String =
        Instant.ofEpochMilli(now).atZone(zone).toLocalDate().toString()

    private fun splitMessage(raw: String): Message {
        val parts = raw.split("\n", limit = 2)
        return Message(parts[0], parts.getOrElse(1) { "" })
    }
}
