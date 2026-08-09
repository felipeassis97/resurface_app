package com.resurface.resurface.ui

import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import com.resurface.resurface.ui.onboarding.PermissionRecoveryScreen
import com.resurface.resurface.ui.theme.ResurfaceMotion
import com.resurface.resurface.ui.theme.rememberReducedMotion

/**
 * Gate de launch: resolve conclusão + permissões ao vivo e roteia pra onboarding, recuperação ou
 * app. Reavalia a cada resume (permissão trocada nas configs não tem callback). Sobe o FGS ao
 * entrar no app. A troca onboarding→app é um crossfade (continuidade "surfacing", P6).
 */
@Composable
fun ResurfaceApp() {
    val appViewModel: AppViewModel = hiltViewModel()
    val route by appViewModel.startRoute.collectAsStateWithLifecycle()
    val statuses by appViewModel.permissionStatuses.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val reduced = rememberReducedMotion()

    LifecycleResumeEffect(Unit) {
        appViewModel.refresh()
        onPauseOrDispose { }
    }

    Crossfade(
        targetState = route,
        animationSpec = tween(if (reduced) 0 else ResurfaceMotion.NudgeEnterMillis, easing = ResurfaceMotion.Emphasized),
        label = "route",
    ) { r ->
        when (r) {
            StartRoute.Loading -> Box(modifier = Modifier.fillMaxSize())
            is StartRoute.Onboarding -> OnboardingFlow(appViewModel, r.step, statuses)
            StartRoute.PermissionRecovery -> PermissionRecoveryScreen(appViewModel)
            StartRoute.Main -> {
                LaunchedEffect(Unit) {
                    context.startForegroundService(Intent(context, MonitorService::class.java))
                }
                MainShell()
            }
        }
    }
}
