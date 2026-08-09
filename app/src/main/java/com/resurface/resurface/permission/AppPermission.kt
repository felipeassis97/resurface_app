package com.resurface.resurface.permission

/**
 * Permissões do app. [Type.SPECIAL] é concedida por tela do sistema; [Type.RUNTIME] pelo diálogo.
 * ACCESSIBILITY é OPCIONAL (D15) — liga o dado de comportamento (F5), mas NÃO entra em [required].
 */
enum class AppPermission(val type: Type) {
    USAGE_ACCESS(Type.SPECIAL),
    NOTIFICATIONS(Type.RUNTIME),
    ACCESSIBILITY(Type.SPECIAL);

    enum class Type { SPECIAL, RUNTIME }

    companion object {
        /** Obrigatórias pra sair do onboarding. Acessibilidade fica de fora (opcional). */
        val required: List<AppPermission> = listOf(USAGE_ACCESS, NOTIFICATIONS)
    }
}
