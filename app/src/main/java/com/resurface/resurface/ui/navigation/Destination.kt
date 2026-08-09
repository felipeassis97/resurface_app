package com.resurface.resurface.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Rotas type-safe. O app abre no [DashboardRoute]; [SettingsRoute] é o hub, alcançado pelo ícone de
 * ajustes; as demais são as sub-telas do hub. Não há bottom nav.
 */
@Serializable
object DashboardRoute

@Serializable
object SettingsRoute

@Serializable
object ProfileRoute

@Serializable
object RemindersRoute

@Serializable
object ScheduleRoute

@Serializable
object WristbandRoute

@Serializable
object DebugRoute
