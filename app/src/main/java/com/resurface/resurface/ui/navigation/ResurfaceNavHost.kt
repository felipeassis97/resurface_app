package com.resurface.resurface.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.resurface.resurface.ui.screens.dashboard.DashboardScreen
import com.resurface.resurface.ui.screens.settings.SettingsScreen

/** Registra as rotas: dashboard (inicial) e ajustes (alcançada pelo ícone da top bar). */
@Composable
fun ResurfaceNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
        modifier = modifier,
    ) {
        composable<DashboardRoute> {
            DashboardScreen(onOpenSettings = { navController.navigate(SettingsRoute) })
        }
        composable<SettingsRoute> { SettingsScreen() }
    }
}
