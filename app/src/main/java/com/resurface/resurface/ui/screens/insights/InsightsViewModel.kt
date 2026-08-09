package com.resurface.resurface.ui.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.data.behavior.BehaviorRepository
import com.resurface.resurface.data.config.TimeProvider
import com.resurface.resurface.data.episode.EpisodeRepository
import com.resurface.resurface.data.outcome.OutcomeRepository
import com.resurface.resurface.domain.BehaviorInput
import com.resurface.resurface.domain.InsightsAggregator
import com.resurface.resurface.domain.InsightsUiState
import com.resurface.resurface.domain.OutcomeInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    episodes: EpisodeRepository,
    outcomes: OutcomeRepository,
    behavior: BehaviorRepository,
    private val time: TimeProvider,
) : ViewModel() {

    private val aggregator = InsightsAggregator()
    private val zone: ZoneId = ZoneId.systemDefault()

    /** Combina as três fontes e agrega no estado do dashboard (a lógica está no aggregator puro). */
    val uiState: StateFlow<InsightsUiState> =
        combine(episodes.history, outcomes.outcomes, behavior.events) { eps, outs, beh ->
            aggregator.aggregate(
                episodes = eps,
                outcomes = outs.map { OutcomeInput(it.appLabel, it.response, it.firedAt) },
                behavior = beh.map { BehaviorInput(it.timestamp, it.hesitated) },
                nowMillis = time.now(),
                zone = zone,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())
}
