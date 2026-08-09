package com.resurface.resurface.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.data.config.ConfigRepository
import com.resurface.resurface.data.onboarding.OnboardingRepository
import com.resurface.resurface.data.profile.ProfileRepository
import com.resurface.resurface.domain.model.Tone
import com.resurface.resurface.permission.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Rascunho do perfil no onboarding + validação (write-through nos repos). */
data class OnboardingProfile(
    val name: String = "",
    val tone: Tone = Tone.GENTIL,
    val hobbies: Set<String> = emptySet(),
    val hobbyFree: String? = null,
    val limitMinutes: Int = ConfigRepository.DEFAULT_LIMIT,
) {
    /** Ao menos um hobby marcado ou o campo livre preenchido (obrigatório pra concluir). */
    val hasHobby: Boolean get() = hobbies.isNotEmpty() || !hobbyFree.isNullOrBlank()

    /** Nome informado (obrigatório pra concluir). */
    val hasName: Boolean get() = name.isNotBlank()

    /** Perfil completo pra concluir: nome + hobby (tom tem default, limite sempre na faixa). */
    val isValid: Boolean get() = hasName && hasHobby
}

/**
 * Dono do rascunho de perfil e da conclusão do onboarding. Separa "o que estou preenchendo" do
 * gate ([com.resurface.resurface.ui.AppViewModel], "pra onde vou"). Escrita é write-through, então
 * processo morto no meio não perde o perfil.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboarding: OnboardingRepository,
    private val profile: ProfileRepository,
    private val config: ConfigRepository,
    private val permissions: PermissionChecker,
) : ViewModel() {

    /** Estado observável do perfil, montado dos repos. */
    val profileState: StateFlow<OnboardingProfile> =
        combine(profile.profile, config.limitMinutes) { prof, limit ->
            OnboardingProfile(
                name = prof.name,
                tone = prof.tone,
                hobbies = prof.hobbies,
                hobbyFree = prof.hobbyFree,
                limitMinutes = limit,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingProfile())

    /** Registra o consentimento (o pager avança localmente; o gate só recomputa no relaunch). */
    fun recordConsent() = viewModelScope.launch { onboarding.recordConsent() }

    /** Grava o nome. */
    fun setName(name: String) = viewModelScope.launch { profile.setName(name) }

    /** Grava o tom. */
    fun setTone(tone: Tone) = viewModelScope.launch { profile.setTone(tone) }

    /** Liga/desliga um hobby, preservando o campo livre. */
    fun toggleHobby(hobby: String) = viewModelScope.launch {
        val current = profile.profile.first()
        val next = if (hobby in current.hobbies) current.hobbies - hobby else current.hobbies + hobby
        profile.setHobbies(next, free = current.hobbyFree)
    }

    /** Grava o campo livre de hobby, preservando os marcados. */
    fun setHobbyFree(free: String) = viewModelScope.launch {
        val current = profile.profile.first()
        profile.setHobbies(current.hobbies, free = free)
    }

    /** Grava o limite (o repositório rejeita fora de 10–60). */
    fun setLimit(minutes: Int) = viewModelScope.launch { config.setLimit(minutes) }

    /**
     * Conclui: exige obrigatórias concedidas + perfil válido, marca `onboardingCompleted` e então
     * chama [onDone] (a UI recomputa a rota do gate).
     */
    fun complete(onDone: () -> Unit) = viewModelScope.launch {
        val valid = profileState.value.isValid
        if (permissions.allRequiredGranted() && valid) {
            onboarding.setCompleted(true)
        }
        onDone()
    }
}
