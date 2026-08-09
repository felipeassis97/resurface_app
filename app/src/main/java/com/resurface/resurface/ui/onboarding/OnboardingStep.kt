package com.resurface.resurface.ui.onboarding

/** Passo inicial do onboarding pra onde o gate manda. */
enum class OnboardingStep {
    /** Tela inicial: o que é + consentimento. */
    WELCOME,

    /** Passos de permissão (o consentimento já foi dado). */
    PERMISSIONS,
}
