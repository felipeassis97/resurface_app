package com.resurface.resurface.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.resurface.resurface.ui.screens.home.HomeScreen
import com.resurface.resurface.ui.screens.insights.InsightsScreen
import com.resurface.resurface.ui.screens.settings.SettingsScreen

/** Registra as rotas type-safe dos três destinos top-level (Home/Insights/Ajustes). */
@Composable
fun ResurfaceNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Destination.start.route,
        modifier = modifier,
    ) {
        composable<HomeRoute> { HomeScreen() }
        composable<InsightsRoute> { InsightsScreen() }
        composable<SettingsRoute> { SettingsScreen() }
    }
}
