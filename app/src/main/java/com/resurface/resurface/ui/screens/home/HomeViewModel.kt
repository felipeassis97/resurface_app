package com.resurface.resurface.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.config.TimeProvider
import com.resurface.resurface.domain.AppLabels
import com.resurface.resurface.domain.model.EpisodePhase
import com.resurface.resurface.domain.model.EpisodeState
import com.resurface.resurface.service.EpisodeStateHolder
import com.resurface.resurface.ui.tickerFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Estado da Home: o contador vivo. */
data class HomeUiState(
    val active: Boolean = false,
    val minutes: Int = 0,
    val appLabel: String = "",
    val pausedToday: Boolean = false,
)

/** Mapeia o estado do episódio + relógio + pausa pro UiState. Puro (testável, sem ticker). */
fun toHomeUiState(state: EpisodeState, nowMillis: Long, paused: Boolean): HomeUiState =
    HomeUiState(
        active = state.phase == EpisodePhase.DENTRO,
        minutes = (state.accumulatedMsAt(nowMillis) / 60_000L).toInt(),
        appLabel = state.currentApp?.let { AppLabels.of(it) } ?: "",
        pausedToday = paused,
    )

@HiltViewModel
class HomeViewModel @Inject constructor(
    holder: EpisodeStateHolder,
    config: ConfigRepository,
    private val time: TimeProvider,
) : ViewModel() {

    /** Contador vivo: o ticker de 1 s força re-emissão; o número vem do estado no instante atual. */
    val uiState: StateFlow<HomeUiState> =
        combine(holder.state, config.pausedToday, tickerFlow(1_000)) { state, paused, _ ->
            toHomeUiState(state, time.now(), paused)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
