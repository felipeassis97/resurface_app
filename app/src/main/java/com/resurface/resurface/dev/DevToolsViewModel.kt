package com.resurface.resurface.dev

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.data.onboarding.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** ViewModel das ferramentas de dev (isolado): aviso de teste + controles de onboarding. */
@HiltViewModel
class DevToolsViewModel @Inject constructor(
    private val trigger: TestAlertTrigger,
    private val debugPrefs: DebugPreferences,
    private val onboarding: OnboardingRepository,
) : ViewModel() {

    /** Toggle "sempre mostrar onboarding no launch" (substitui o reset hardcoded). */
    val alwaysShowOnboarding: StateFlow<Boolean> =
        debugPrefs.alwaysShowOnboarding.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Dispara o aviso de teste (fire-and-forget). */
    fun onTestAlert() {
        viewModelScope.launch { trigger.fire() }
    }

    /** Liga/desliga o "sempre mostrar onboarding". */
    fun onToggleAlwaysShowOnboarding(value: Boolean) {
        viewModelScope.launch { debugPrefs.setAlwaysShowOnboarding(value) }
    }

    /** Zera consentimento + conclusão do onboarding agora. */
    fun onResetOnboarding() {
        viewModelScope.launch { onboarding.resetForTesting() }
    }
}
