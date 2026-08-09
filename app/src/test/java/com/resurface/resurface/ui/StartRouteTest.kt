package com.resurface.resurface.ui

import com.resurface.resurface.data.onboarding.OnboardingState
import com.resurface.resurface.ui.onboarding.OnboardingStep
import org.junit.Assert.assertEquals
import org.junit.Test

class StartRouteTest {

    private fun state(consent: Boolean) = OnboardingState(consentGiven = consent, onboardingCompleted = false)

    /** Sem consentimento → onboarding na tela inicial. */
    @Test
    fun `sem consentimento vai pro welcome`() {
        assertEquals(
            StartRoute.Onboarding(OnboardingStep.WELCOME),
            computeStartRoute(state(consent = false), allRequiredGranted = true),
        )
    }

    /** Consentido mas faltando obrigatória → onboarding nas permissões. */
    @Test
    fun `consentido sem permissao vai pras permissoes`() {
        assertEquals(
            StartRoute.Onboarding(OnboardingStep.PERMISSIONS),
            computeStartRoute(state(consent = true), allRequiredGranted = false),
        )
    }

    /** Consentido e tudo concedido → app. */
    @Test
    fun `tudo ok vai pro main`() {
        assertEquals(StartRoute.Main, computeStartRoute(state(consent = true), allRequiredGranted = true))
    }
}
