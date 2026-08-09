package com.resurface.resurface.ui

import com.resurface.resurface.data.onboarding.OnboardingState
import com.resurface.resurface.ui.onboarding.OnboardingStep

/** Pra onde o gate de launch manda o usuário na abertura. */
sealed interface StartRoute {
    /** Ainda resolvendo (consentimento/permissões). */
    data object Loading : StartRoute

    /** Onboarding, a partir de [step]. */
    data class Onboarding(val step: OnboardingStep) : StartRoute

    /** App principal. */
    data object Main : StartRoute
}

/**
 * Decide a rota inicial (pura, testável): sem consentimento → WELCOME; consentido mas faltando
 * obrigatória → PERMISSIONS; tudo ok → Main. Acessibilidade não entra (opcional, D15).
 */
fun computeStartRoute(state: OnboardingState, allRequiredGranted: Boolean): StartRoute = when {
    !state.consentGiven -> StartRoute.Onboarding(OnboardingStep.WELCOME)
    !allRequiredGranted -> StartRoute.Onboarding(OnboardingStep.PERMISSIONS)
    else -> StartRoute.Main
}
