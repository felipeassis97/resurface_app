package com.resurface.resurface.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.profile.ProfileRepository
import com.resurface.resurface.domain.model.Tone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado dos ajustes: limite, pausa, tom e hobbies. */
data class SettingsUiState(
    val limitMinutes: Int = ConfigRepository.DEFAULT_LIMIT,
    val pausedToday: Boolean = false,
    val tone: Tone = Tone.GENTIL,
    val hobbies: Set<String> = emptySet(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val config: ConfigRepository,
    private val profile: ProfileRepository,
) : ViewModel() {

    /** Espelha limite/pausa (config) + tom/hobbies (perfil). Repositório é a fonte da verdade. */
    val uiState: StateFlow<SettingsUiState> =
        combine(config.limitMinutes, config.pausedToday, profile.profile) { limit, paused, prof ->
            SettingsUiState(limit, paused, prof.tone, prof.hobbies)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** Grava o novo limite (o repositório rejeita fora de 10–60). */
    fun onSetLimit(minutes: Int) {
        viewModelScope.launch { config.setLimit(minutes) }
    }

    /** Ativa "pausar por hoje" (D11). */
    fun onPauseToday() {
        viewModelScope.launch { config.pauseForToday() }
    }

    /** Troca o tom da mensagem. */
    fun onSetTone(tone: Tone) {
        viewModelScope.launch { profile.setTone(tone) }
    }

    /** Liga/desliga um hobby. */
    fun onToggleHobby(hobby: String) {
        viewModelScope.launch {
            val current = profile.profile.first().hobbies
            val next = if (hobby in current) current - hobby else current + hobby
            profile.setHobbies(next, free = null)
        }
    }
}
