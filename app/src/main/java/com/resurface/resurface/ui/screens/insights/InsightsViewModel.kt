package com.resurface.resurface.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.data.episode.EpisodeRepository
import com.resurface.resurface.data.outcome.AlertOutcomeEntity
import com.resurface.resurface.data.outcome.AlertResponse
import com.resurface.resurface.data.outcome.OutcomeRepository
import com.resurface.resurface.domain.AppLabels
import com.resurface.resurface.domain.model.ClosedEpisode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Uma linha de episódio no histórico. */
data class EpisodeRow(val durationMinutes: Int, val apps: String, val startedAt: Long)

/** Uma linha de aviso com a resposta ("era hora" / "agora não" / "sem resposta"). */
data class AlertRow(val appLabel: String, val response: String, val firedAt: Long)

/** Estado da tela de observações. `eraHoraPct` é null quando nenhum aviso foi respondido. */
data class InsightsUiState(
    val episodes: List<EpisodeRow> = emptyList(),
    val alerts: List<AlertRow> = emptyList(),
    val eraHoraPct: Int? = null,
)

/** Rótulo humano da resposta guardada. */
private fun responseLabel(stored: String?): String = when (stored) {
    AlertResponse.ERA_HORA.stored -> "era hora"
    AlertResponse.AGORA_NAO.stored -> "agora não"
    else -> "sem resposta"
}

/** Mapeia episódios + outcomes pro UiState e deriva a S2 (era hora % entre respondidos). Puro. */
fun toInsightsUiState(episodes: List<ClosedEpisode>, outcomes: List<AlertOutcomeEntity>): InsightsUiState {
    val epRows = episodes.map {
        EpisodeRow(
            durationMinutes = (it.accumulatedMs / 60_000L).toInt(),
            apps = it.apps.map(AppLabels::of).sorted().joinToString(" · "),
            startedAt = it.startedAt,
        )
    }
    val alertRows = outcomes.map { AlertRow(it.appLabel, responseLabel(it.response), it.firedAt) }
    val responded = outcomes.filter { it.response != null }
    val pct = if (responded.isEmpty()) null
    else responded.count { it.response == AlertResponse.ERA_HORA.stored } * 100 / responded.size
    return InsightsUiState(epRows, alertRows, pct)
}

@HiltViewModel
class InsightsViewModel @Inject constructor(
    episodes: EpisodeRepository,
    outcomes: OutcomeRepository,
) : ViewModel() {

    /** Combina histórico + outcomes numa única fonte de estado da tela. */
    val uiState: StateFlow<InsightsUiState> =
        combine(episodes.history, outcomes.outcomes) { eps, outs -> toInsightsUiState(eps, outs) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())
}
