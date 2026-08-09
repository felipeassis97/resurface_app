package com.resurface.resurface.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.service.MonitorService
import com.resurface.resurface.ui.onboarding.OnboardingFlow

/**
 * Gate de launch: resolve consentimento + permissões ao vivo e roteia pra onboarding ou app.
 * Reavalia a cada resume (permissão trocada nas configs não tem callback). Sobe o FGS ao entrar
 * no app (D-5).
 */
@Composable
fun ResurfaceApp() {
    val appViewModel: AppViewModel = hiltViewModel()
    val route by appViewModel.startRoute.collectAsStateWithLifecycle()
    val statuses by appViewModel.permissionStatuses.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        appViewModel.refresh()
        onPauseOrDispose { }
    }

    when (val r = route) {
        StartRoute.Loading -> Box(modifier = Modifier.fillMaxSize())
        is StartRoute.Onboarding -> OnboardingFlow(appViewModel, r.step, statuses)
        StartRoute.Main -> {
            LaunchedEffect(Unit) {
                context.startForegroundService(Intent(context, MonitorService::class.java))
            }
            MainShell()
        }
    }
}
