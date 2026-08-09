package com.resurface.resurface.ui.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Box
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
 * Onboarding paginado — um conceito por tela (welcome → obrigatórias → bateria → perfil →
 * acessibilidade → conclusão). O pager é dirigido por botão (não por swipe), pra as obrigatórias
 * poderem travar o avanço até serem concedidas.
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

    val usageOk = permissionStatuses[AppPermission.USAGE_ACCESS] == true
    val notifOk = permissionStatuses[AppPermission.NOTIFICATIONS] == true
    val a11yOk = permissionStatuses[AppPermission.ACCESSIBILITY] == true

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
                    title = "Um relógio pra uma experiência feita pra não ter nenhum.",
                    body = "Avisa quando você está há um tempo no vídeo curto — sem bloquear, sem julgar. Tudo fica no aparelho: sem conta, sem servidor, sem nuvem.",
                    primaryLabel = "Começar",
                    onPrimary = { onboardingViewModel.recordConsent(); next() },
                )

                OnboardingStep.USAGE -> OnboardingScaffold(
                    stepIndex = OnboardingStep.USAGE.ordinal,
                    icon = Icons.Filled.QueryStats,
                    title = "Acesso ao uso",
                    body = "Pra saber quais apps estão abertos e por quanto tempo. É o contador — sem isso, não há produto.",
                    primaryLabel = if (usageOk) "Avançar" else "Abrir configurações",
                    onPrimary = {
                        if (usageOk) next()
                        else appViewModel.settingsIntentFor(AppPermission.USAGE_ACCESS)?.let(context::startActivity)
                    },
                ) { reducedMotion -> PermissionStatusChip(usageOk, reducedMotion) }

                OnboardingStep.NOTIFICATIONS -> OnboardingScaffold(
                    stepIndex = OnboardingStep.NOTIFICATIONS.ordinal,
                    icon = Icons.Filled.Notifications,
                    title = "Notificações",
                    body = "Pra poder te avisar. Sem isso, o aviso não aparece — e o aviso é o produto.",
                    primaryLabel = if (notifOk) "Avançar" else "Permitir",
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
                    title = "Não deixar o aviso atrasar",
                    body = "O Samsung congela apps em segundo plano. Peça a isenção de bateria e, nas configs, adicione o Resurface em \"Apps que nunca dormem\". Dá pra fazer depois.",
                    primaryLabel = "Pedir isenção",
                    onPrimary = {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                .setData(Uri.parse("package:${context.packageName}"))
                        )
                    },
                    secondaryLabel = "Continuar",
                    onSecondary = { next() },
                )

                OnboardingStep.TONE -> ToneStep(
                    stepIndex = OnboardingStep.TONE.ordinal,
                    selected = profile.tone,
                    onSelect = onboardingViewModel::setTone,
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
                    title = "Contar vídeos (opcional)",
                    body = "Liga a contagem de vídeos e a hesitação. Em app fora da loja, ative antes \"permitir configurações restritas\" em Info do app. Pode pular — o app funciona sem isso.",
                    primaryLabel = if (a11yOk) "Avançar" else "Ligar acessibilidade",
                    onPrimary = {
                        if (a11yOk) next()
                        else appViewModel.settingsIntentFor(AppPermission.ACCESSIBILITY)?.let(context::startActivity)
                    },
                    secondaryLabel = if (a11yOk) null else "Pular",
                    onSecondary = { next() },
                ) { reducedMotion -> PermissionStatusChip(a11yOk, reducedMotion) }

                OnboardingStep.DONE -> OnboardingScaffold(
                    stepIndex = OnboardingStep.DONE.ordinal,
                    icon = Icons.Filled.CheckCircle,
                    title = "Tudo pronto",
                    body = "O Resurface vai ficar quieto e só aparecer quando fizer sentido.",
                    primaryLabel = "Concluir",
                    onPrimary = { onboardingViewModel.complete { appViewModel.refresh() } },
                )
            }
        }
    }
}
