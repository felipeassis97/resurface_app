package com.resurface.resurface.ui.onboarding

/**
 * Sequência ordenada do onboarding — um conceito por tela. O pager percorre [entries] na ordem
 * declarada. O progresso é o índice na sequência (structure-is-information: é uma sequência real).
 *
 * Ordem (PRODUTO §5.1): welcome → obrigatórias → bateria (adiável) → perfil → acessibilidade
 * (opcional) → conclusão.
 */
enum class OnboardingStep {
    /** O que é o app + promessa de privacidade + consentimento. */
    WELCOME,

    /** Acesso ao uso (obrigatória, tela do sistema). */
    USAGE,

    /** Notificações (obrigatória, diálogo runtime Android 13+). */
    NOTIFICATIONS,

    /** Isenção de bateria + passo do Samsung (adiável). */
    BATTERY,

    /** Perfil: tom (obrigatório). */
    TONE,

    /** Perfil: hobbies, ≥1 (obrigatório). */
    HOBBIES,

    /** Perfil: limite de minutos 10–60 (obrigatório). */
    LIMIT,

    /** Acessibilidade (opcional, pulável, D15). */
    ACCESSIBILITY,

    /** Conclusão — marca o onboarding e libera o app. */
    DONE;

    companion object {
        /** Todos os passos, na ordem. */
        val steps: List<OnboardingStep> = entries

        /** Total de passos, pro indicador de progresso. */
        val count: Int = entries.size
    }
}
