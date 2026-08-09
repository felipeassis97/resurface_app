package com.resurface.resurface.permission

/**
 * Permissões obrigatórias do F1. [Type.SPECIAL] é concedida por tela do sistema (Settings);
 * [Type.RUNTIME] pelo diálogo de runtime. Acessibilidade (opcional, D15) fica pro módulo de F5.
 */
enum class AppPermission(val type: Type) {
    USAGE_ACCESS(Type.SPECIAL),
    NOTIFICATIONS(Type.RUNTIME);

    enum class Type { SPECIAL, RUNTIME }

    companion object {
        /** Todas as permissões necessárias pra sair do onboarding no F1. */
        val required: List<AppPermission> = entries.toList()
    }
}
