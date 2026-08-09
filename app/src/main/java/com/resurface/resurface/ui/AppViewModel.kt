package com.resurface.resurface.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resurface.resurface.data.onboarding.OnboardingRepository
import com.resurface.resurface.permission.AppPermission
import com.resurface.resurface.permission.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Gate de launch: resolve consentimento + permissões ao vivo e roteia (D-1). */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val onboarding: OnboardingRepository,
    private val permissions: PermissionChecker,
) : ViewModel() {

    private val _startRoute = MutableStateFlow<StartRoute>(StartRoute.Loading)
    val startRoute: StateFlow<StartRoute> = _startRoute.asStateFlow()

    private val _permissionStatuses = MutableStateFlow(permissions.statuses())
    val permissionStatuses: StateFlow<Map<AppPermission, Boolean>> = _permissionStatuses.asStateFlow()

    init {
        refresh()
    }

    /** Reavalia o status ao vivo e recomputa a rota (chamar em todo resume). */
    fun refresh() {
        _permissionStatuses.value = permissions.statuses()
        viewModelScope.launch {
            _startRoute.value = computeStartRoute(onboarding.state.first(), permissions.allRequiredGranted())
        }
    }

    /** Registra o consentimento e reavalia. */
    fun recordConsent() = viewModelScope.launch {
        onboarding.recordConsent()
        refresh()
    }

    /** Conclui o onboarding se as obrigatórias estão concedidas; reavalia. */
    fun completeOnboarding() = viewModelScope.launch {
        if (permissions.allRequiredGranted()) onboarding.setCompleted(true)
        refresh()
    }

    /** Intent pra tela de concessão de uma permissão especial; null pra runtime. */
    fun settingsIntentFor(permission: AppPermission): Intent? = permissions.settingsIntent(permission)
}
