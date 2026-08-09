package com.resurface.resurface.ui

import com.resurface.resurface.data.onboarding.OnboardingState
import com.resurface.resurface.ui.onboarding.OnboardingStep
import org.junit.Assert.assertEquals
import org.junit.Test

class StartRouteTest {

    private fun state(consent: Boolean, completed: Boolean) =
        OnboardingState(consentGiven = consent, onboardingCompleted = completed)

    /** Não concluído + sem consentimento → onboarding no welcome. */
    @Test
    fun `sem consentimento vai pro welcome`() {
        assertEquals(
            StartRoute.Onboarding(OnboardingStep.WELCOME),
            computeStartRoute(state(consent = false, completed = false), allRequiredGranted = false),
        )
    }

    /** Não concluído + consentido + faltando obrigatória → onboarding nas permissões (acesso ao uso). */
    @Test
    fun `consentido sem permissao comeca nas permissoes`() {
        assertEquals(
            StartRoute.Onboarding(OnboardingStep.USAGE),
            computeStartRoute(state(consent = true, completed = false), allRequiredGranted = false),
        )
    }

    /** Não concluído + consentido + obrigatórias ok → segue pra bateria/perfil. */
    @Test
    fun `consentido com permissoes segue pra bateria`() {
        assertEquals(
            StartRoute.Onboarding(OnboardingStep.BATTERY),
            computeStartRoute(state(consent = true, completed = false), allRequiredGranted = true),
        )
    }

    /** Concluído + tudo concedido → app. */
    @Test
    fun `concluido e tudo ok vai pro main`() {
        assertEquals(
            StartRoute.Main,
            computeStartRoute(state(consent = true, completed = true), allRequiredGranted = true),
        )
    }

    /** Concluído mas obrigatória revogada depois → recuperação (não reabre onboarding). */
    @Test
    fun `concluido mas faltando obrigatoria vai pra recuperacao`() {
        assertEquals(
            StartRoute.PermissionRecovery,
            computeStartRoute(state(consent = true, completed = true), allRequiredGranted = false),
        )
    }

    /** firstPendingStep cobre os três pontos de entrada. */
    @Test
    fun `firstPendingStep deriva o passo inicial`() {
        assertEquals(OnboardingStep.WELCOME, firstPendingStep(consentGiven = false, allRequiredGranted = false))
        assertEquals(OnboardingStep.USAGE, firstPendingStep(consentGiven = true, allRequiredGranted = false))
        assertEquals(OnboardingStep.BATTERY, firstPendingStep(consentGiven = true, allRequiredGranted = true))
    }
}
