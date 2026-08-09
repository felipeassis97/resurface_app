package com.resurface.resurface.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.resurface.resurface.ui.navigation.ResurfaceNavHost

/** App principal: uma tela (dashboard) + ajustes secundária. Sem bottom navigation. */
@Composable
fun MainShell() {
    val navController = rememberNavController()
    ResurfaceNavHost(navController = navController)
}
