package com.resurface.resurface.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Schedule
import com.resurface.resurface.permission.AppPermission
import com.resurface.resurface.ui.AppViewModel
import com.resurface.resurface.ui.theme.rememberReducedMotion
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Onboarding paginado, um conceito por tela (welcome, obrigatórias, bateria, perfil, acessibilidade,
 * conclusão). O pager é dirigido por botão (não por swipe), pra as obrigatórias poderem travar o
 * avanço até serem concedidas. Copy em inglês, sem travessões.
 */
@Composable
fun OnboardingFlow(
    appViewModel: AppViewModel,
    initialStep: OnboardingStep,
    permissionStatuses: Map<AppPermission, Boolean>,
) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val profile by onboardingViewModel.profileState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pagerState = rememberPagerState(initialPage = initialStep.ordinal) { OnboardingStep.count }
    fun next() {
        val n = pagerState.currentPage + 1
        if (n < OnboardingStep.count) scope.launch { pagerState.animateScrollToPage(n) }
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        appViewModel.refresh()
    }

    fun openBatteryExemption() {
        val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            .setData(Uri.parse("package:${context.packageName}"))
        runCatching { context.startActivity(direct) }.onFailure {
            runCatching { context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
        }
    }

    val usageOk = permissionStatuses[AppPermission.USAGE_ACCESS] == true
    val notifOk = permissionStatuses[AppPermission.NOTIFICATIONS] == true
    val a11yOk = permissionStatuses[AppPermission.ACCESSIBILITY] == true
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val batteryExempt = powerManager.isIgnoringBatteryOptimizations(context.packageName)

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        val reduced = rememberReducedMotion()
        val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        val pageModifier = if (reduced) Modifier else Modifier.graphicsLayer {
            val a = abs(offset).coerceIn(0f, 1f)
            alpha = 1f - a * 0.4f
            val s = 1f - a * 0.05f
            scaleX = s; scaleY = s
        }
        Box(pageModifier) {
            when (OnboardingStep.steps[page]) {
                OnboardingStep.WELCOME -> OnboardingScaffold(
                    stepIndex = 0,
                    icon = Icons.Filled.Schedule,
                    title = "A clock for an experience built to have none.",
                    body = "It lets you know when you have been on short video for a while. No blocking, no judgment. Everything stays on your phone: no account, no server, no cloud.",
                    primaryLabel = "Get started",
                    onPrimary = { onboardingViewModel.recordConsent(); next() },
                )

                OnboardingStep.USAGE -> OnboardingScaffold(
                    stepIndex = OnboardingStep.USAGE.ordinal,
                    icon = Icons.Filled.QueryStats,
                    title = "Usage access",
                    body = "So it knows which apps are open and for how long. This is the counter. Without it there is no product.",
                    primaryLabel = if (usageOk) "Next" else "Open settings",
                    onPrimary = {
                        if (usageOk) next()
                        else appViewModel.settingsIntentFor(AppPermission.USAGE_ACCESS)?.let(context::startActivity)
                    },
                ) { reducedMotion -> PermissionStatusChip(usageOk, reducedMotion) }

                OnboardingStep.NOTIFICATIONS -> OnboardingScaffold(
                    stepIndex = OnboardingStep.NOTIFICATIONS.ordinal,
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    body = "So it can reach you. Without this the alert never shows, and the alert is the product.",
                    primaryLabel = if (notifOk) "Next" else "Allow",
                    onPrimary = {
                        if (notifOk) next()
                        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else next()
                    },
                ) { reducedMotion -> PermissionStatusChip(notifOk, reducedMotion) }

                OnboardingStep.BATTERY -> OnboardingScaffold(
                    stepIndex = OnboardingStep.BATTERY.ordinal,
                    icon = Icons.Filled.BatteryChargingFull,
                    title = "Keep alerts on time",
                    body = "Samsung freezes background apps. Ask for the battery exemption and, in settings, add Resurface to \"Never sleeping apps\". You can do this later.",
                    primaryLabel = if (batteryExempt) "Continue" else "Request exemption",
                    onPrimary = { if (batteryExempt) next() else openBatteryExemption() },
                    secondaryLabel = if (batteryExempt) null else "Continue",
                    onSecondary = { next() },
                ) { reducedMotion -> if (batteryExempt) PermissionStatusChip(true, reducedMotion) }

                OnboardingStep.TONE -> ToneStep(
                    stepIndex = OnboardingStep.TONE.ordinal,
                    name = profile.name,
                    onName = onboardingViewModel::setName,
                    selected = profile.tone,
                    onSelect = onboardingViewModel::setTone,
                    canAdvance = profile.hasName,
                    onNext = { next() },
                )

                OnboardingStep.HOBBIES -> HobbiesStep(
                    stepIndex = OnboardingStep.HOBBIES.ordinal,
                    hobbies = profile.hobbies,
                    hobbyFree = profile.hobbyFree,
                    onToggle = onboardingViewModel::toggleHobby,
                    onFree = onboardingViewModel::setHobbyFree,
                    canAdvance = profile.hasHobby,
                    onNext = { next() },
                )

                OnboardingStep.LIMIT -> LimitStep(
                    stepIndex = OnboardingStep.LIMIT.ordinal,
                    minutes = profile.limitMinutes,
                    onSet = onboardingViewModel::setLimit,
                    onNext = { next() },
                )

                OnboardingStep.ACCESSIBILITY -> OnboardingScaffold(
                    stepIndex = OnboardingStep.ACCESSIBILITY.ordinal,
                    icon = Icons.Filled.Accessibility,
                    title = "Count videos (optional)",
                    body = "Turns on video counting and hesitation. On a sideloaded app, first enable \"Allow restricted settings\" in App info. You can skip this, the app works without it.",
                    primaryLabel = if (a11yOk) "Continue" else "Turn on accessibility",
                    onPrimary = {
                        if (a11yOk) next()
                        else appViewModel.settingsIntentFor(AppPermission.ACCESSIBILITY)?.let(context::startActivity)
                    },
                    secondaryLabel = if (a11yOk) null else "Skip",
                    onSecondary = { next() },
                ) { reducedMotion -> PermissionStatusChip(a11yOk, reducedMotion) }

                OnboardingStep.DONE -> OnboardingScaffold(
                    stepIndex = OnboardingStep.DONE.ordinal,
                    icon = Icons.Filled.CheckCircle,
                    title = "All set",
                    body = "Resurface will stay quiet and only show up when it makes sense.",
                    primaryLabel = "Finish",
                    onPrimary = { onboardingViewModel.complete { appViewModel.refresh() } },
                )
            }
        }
    }
}
