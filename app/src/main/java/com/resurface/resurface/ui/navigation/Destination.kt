package com.resurface.resurface.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Rotas type-safe. O app abre no [DashboardRoute] (tela única = contador vivo + observações);
 * [SettingsRoute] é secundária, alcançada pelo ícone de ajustes na top bar. Não há mais abas.
 */
@Serializable
object DashboardRoute

@Serializable
object SettingsRoute
