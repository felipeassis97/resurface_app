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

    /** Onboarding já concluído, mas uma obrigatória foi revogada depois (recuperação). */
    data object PermissionRecovery : StartRoute
}

/**
 * Decide a rota inicial (pura, testável), rotando por **conclusão persistida** do onboarding:
 * - não concluído → onboarding, a partir do passo pendente ([firstPendingStep]);
 * - concluído e obrigatórias ok → app;
 * - concluído mas faltando obrigatória → recuperação (nunca reabre o onboarding).
 *
 * Acessibilidade não entra (opcional, D15).
 */
fun computeStartRoute(state: OnboardingState, allRequiredGranted: Boolean): StartRoute = when {
    !state.onboardingCompleted -> StartRoute.Onboarding(firstPendingStep(state.consentGiven, allRequiredGranted))
    allRequiredGranted -> StartRoute.Main
    else -> StartRoute.PermissionRecovery
}

/**
 * Passo inicial do onboarding ainda não concluído: sem consentimento → welcome; consentido mas
 * faltando obrigatória → começa nas permissões (acesso ao uso); consentido e obrigatórias ok →
 * segue pro passo de bateria (perfil vem depois). O pager cuida da navegação daí em diante.
 */
fun firstPendingStep(consentGiven: Boolean, allRequiredGranted: Boolean): OnboardingStep = when {
    !consentGiven -> OnboardingStep.WELCOME
    !allRequiredGranted -> OnboardingStep.USAGE
    else -> OnboardingStep.BATTERY
}
