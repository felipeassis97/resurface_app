package com.resurface.resurface.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.resurface.resurface.dev.DebugScreen
import com.resurface.resurface.ui.screens.dashboard.DashboardScreen
import com.resurface.resurface.ui.screens.settings.ProfileScreen
import com.resurface.resurface.ui.screens.settings.RemindersScreen
import com.resurface.resurface.ui.screens.settings.ScheduleScreen
import com.resurface.resurface.ui.screens.settings.SettingsHubScreen
import com.resurface.resurface.ui.screens.settings.WristbandScreen

/** Rotas: dashboard (inicial), hub de ajustes e suas sub-telas. */
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
        composable<SettingsRoute> {
            SettingsHubScreen(
                onBack = navController::navigateUp,
                onProfile = { navController.navigate(ProfileRoute) },
                onReminders = { navController.navigate(RemindersRoute) },
                onSchedule = { navController.navigate(ScheduleRoute) },
                onWristband = { navController.navigate(WristbandRoute) },
                onDebug = { navController.navigate(DebugRoute) },
            )
        }
        composable<ProfileRoute> { ProfileScreen(onBack = navController::navigateUp) }
        composable<RemindersRoute> { RemindersScreen(onBack = navController::navigateUp) }
        composable<ScheduleRoute> { ScheduleScreen(onBack = navController::navigateUp) }
        composable<WristbandRoute> { WristbandScreen(onBack = navController::navigateUp) }
        composable<DebugRoute> { DebugScreen(onBack = navController::navigateUp) }
    }
}
