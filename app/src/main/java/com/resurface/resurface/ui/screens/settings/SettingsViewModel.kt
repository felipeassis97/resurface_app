package com.resurface.resurface.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.data.config.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado dos ajustes: limite atual e se está pausado hoje. */
data class SettingsUiState(
    val limitMinutes: Int = ConfigRepository.DEFAULT_LIMIT,
    val pausedToday: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val config: ConfigRepository,
) : ViewModel() {

    /** Espelha o que está gravado; o repositório é a fonte da verdade (sem flip otimista). */
    val uiState: StateFlow<SettingsUiState> =
        combine(config.limitMinutes, config.pausedToday) { limit, paused ->
            SettingsUiState(limit, paused)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** Grava o novo limite (o repositório rejeita fora de 10–60). */
    fun onSetLimit(minutes: Int) {
        viewModelScope.launch { config.setLimit(minutes) }
    }

    /** Ativa "pausar por hoje" (D11). */
    fun onPauseToday() {
        viewModelScope.launch { config.pauseForToday() }
    }
}
