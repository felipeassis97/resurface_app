package com.resurface.resurface.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.ble.WristbandConnectionState
import com.resurface.resurface.ble.WristbandLink
import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.profile.ProfileRepository
import com.resurface.resurface.data.wristband.WristbandPreferences
import com.resurface.resurface.domain.model.Schedule
import com.resurface.resurface.domain.model.Tone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

/** Estado dos ajustes: limite, pausa, tom, hobbies, janela ativa e intensidade da pulseira. */
data class SettingsUiState(
    val limitMinutes: Int = ConfigRepository.DEFAULT_LIMIT,
    val pausedToday: Boolean = false,
    val tone: Tone = Tone.GENTIL,
    val hobbies: Set<String> = emptySet(),
    val schedule: Schedule = Schedule(),
    val intensity: Int? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val config: ConfigRepository,
    private val profile: ProfileRepository,
    private val wristband: WristbandLink,
    private val wristbandPrefs: WristbandPreferences,
) : ViewModel() {

    /** Espelha config + perfil + intensidade da pulseira. Repositório é a fonte da verdade. */
    val uiState: StateFlow<SettingsUiState> =
        combine(config.limitMinutes, config.pausedToday, config.schedule, profile.profile, wristbandPrefs.intensity) { limit, paused, sched, prof, intensity ->
            SettingsUiState(limit, paused, prof.tone, prof.hobbies, sched, intensity)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** Estado do link BLE da pulseira, observado pela UI. */
    val wristbandState: StateFlow<WristbandConnectionState> = wristband.state

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

    /** Liga/desliga um dia da janela ativa. */
    fun onToggleDay(day: DayOfWeek) {
        viewModelScope.launch {
            val s = config.schedule.first()
            val days = if (day in s.days) s.days - day else s.days + day
            config.setSchedule(s.copy(days = days))
        }
    }

    /** Ajusta a faixa de horário (minutos do dia) da janela ativa. */
    fun onSetWindow(startMinute: Int, endMinute: Int) {
        viewModelScope.launch {
            config.setSchedule(config.schedule.first().copy(startMinute = startMinute, endMinute = endMinute))
        }
    }

    /** Pareamento mínimo: faz scan e conecta ao primeiro device válido (app pessoal, 1 pulseira). */
    fun onPairWristband() {
        viewModelScope.launch {
            wristband.startScan()
            // Conecta ao primeiro match que aparecer; para de esperar quando houver um.
            val first = wristband.scanResults.first { it.isNotEmpty() }.first()
            wristband.stopScan()
            wristband.connect(first.address)
        }
    }

    /** Ajusta a intensidade do pulso do aviso (null = auto). */
    fun onSetIntensity(value: Int?) {
        viewModelScope.launch { wristbandPrefs.setIntensity(value) }
    }
}
