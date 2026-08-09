package com.resurface.resurface.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.data.behavior.BehaviorRepository
import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.config.TimeProvider
import com.resurface.resurface.data.episode.EpisodeRepository
import com.resurface.resurface.data.outcome.OutcomeRepository
import com.resurface.resurface.domain.AppLabels
import com.resurface.resurface.domain.BehaviorInput
import com.resurface.resurface.domain.InsightsAggregator
import com.resurface.resurface.domain.InsightsUiState
import com.resurface.resurface.domain.OutcomeInput
import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.EpisodeState
import com.resurface.resurface.service.EpisodeStateHolder
import com.resurface.resurface.ui.tickerFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

/** Tudo que o dashboard mostra: o estado ao vivo (hero) + as observações da semana. */
data class DashboardUiState(
    val live: LiveState = LiveState(),
    val insights: InsightsUiState = InsightsUiState(),
)

/**
 * Funde o contador vivo (Home) com o dashboard (Insights) numa fonte só. A lógica pura vem de
 * [toLiveState] e do [InsightsAggregator]; aqui só combinamos os flows.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    holder: EpisodeStateHolder,
    episodes: EpisodeRepository,
    outcomes: OutcomeRepository,
    behavior: BehaviorRepository,
    config: ConfigRepository,
    private val time: TimeProvider,
) : ViewModel() {

    private val aggregator = InsightsAggregator()
    private val zone: ZoneId = ZoneId.systemDefault()

    /** Contador vivo: o ticker de 1 s força re-emissão; o número vem do estado no instante atual. */
    private val live = combine(holder.state, config.pausedToday, tickerFlow(1_000)) { state, paused, _ ->
        toLiveState(state, time.now(), paused)
    }

    /** Observações: combina as três fontes e agrega (lógica no aggregator puro). */
    private val insights = combine(episodes.history, outcomes.outcomes, behavior.events) { eps, outs, beh ->
        aggregator.aggregate(
            episodes = eps,
            outcomes = outs.map { OutcomeInput(it.appLabel, it.response, it.firedAt) },
            behavior = beh.map { BehaviorInput(it.timestamp, it.hesitated) },
            nowMillis = time.now(),
            zone = zone,
        )
    }

    val uiState: StateFlow<DashboardUiState> =
        combine(live, insights) { l, i -> DashboardUiState(l, i) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
