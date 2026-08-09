package com.resurface.resurface.ui.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.resurface.resurface.permission.AppPermission
import com.resurface.resurface.ui.AppViewModel
import com.resurface.resurface.ui.theme.Spacing

/** Onboarding: consentimento e depois as permissões. Cada passo mostra o status ao vivo. */
@Composable
fun OnboardingFlow(
    appViewModel: AppViewModel,
    initialStep: OnboardingStep,
    permissionStatuses: Map<AppPermission, Boolean>,
) {
    when (initialStep) {
        OnboardingStep.WELCOME -> WelcomeScreen(onConsent = { appViewModel.recordConsent() })
        OnboardingStep.PERMISSIONS -> PermissionsScreen(appViewModel, permissionStatuses)
    }
}

/** Tela inicial: o que é + a promessa de privacidade (P4) + consentimento. */
@Composable
private fun WelcomeScreen(onConsent: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.space6).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.space4),
    ) {
        Text("Resurface", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        Text(
            "Um relógio para uma experiência feita pra não ter nenhum. Avisa quando você está há um tempo no vídeo curto — sem bloquear, sem julgar.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "Tudo fica no aparelho. Sem conta, sem servidor, sem nuvem. Você pode apagar tudo a qualquer momento.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onConsent, modifier = Modifier.fillMaxWidth()) { Text("Começar") }
    }
}

/** Passos de permissão, com status ao vivo e ação por passo. */
@Composable
private fun PermissionsScreen(appViewModel: AppViewModel, statuses: Map<AppPermission, Boolean>) {
    val context = LocalContext.current
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        appViewModel.refresh()
    }
    val usageOk = statuses[AppPermission.USAGE_ACCESS] == true
    val notifOk = statuses[AppPermission.NOTIFICATIONS] == true
    val a11yOk = statuses[AppPermission.ACCESSIBILITY] == true

    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.space6).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.space4),
    ) {
        Text("Permissões", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)

        PermissionCard(
            title = "Acesso ao uso",
            body = "Pra saber quais apps estão abertos e por quanto tempo. É o contador — sem isso não há produto.",
            granted = usageOk,
            action = "Conceder",
            onAction = { appViewModel.settingsIntentFor(AppPermission.USAGE_ACCESS)?.let(context::startActivity) },
        )
        PermissionCard(
            title = "Notificações",
            body = "Pra poder te avisar. Sem isso, o aviso não aparece.",
            granted = notifOk,
            action = "Permitir",
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
        )
        PermissionCard(
            title = "Bateria",
            body = "O Samsung congela apps em segundo plano. Peça a isenção e, nas configs, adicione o Resurface em \"Apps que nunca dormem\".",
            granted = null,
            action = "Abrir isenção",
            onAction = {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        .setData(Uri.parse("package:${context.packageName}"))
                )
            },
        )
        PermissionCard(
            title = "Acessibilidade (opcional)",
            body = "Liga a contagem de vídeos e a hesitação. Em app fora da loja, ative antes \"permitir configurações restritas\" em Info do app. Dá pra pular.",
            granted = a11yOk,
            action = "Ligar",
            onAction = { appViewModel.settingsIntentFor(AppPermission.ACCESSIBILITY)?.let(context::startActivity) },
        )

        Button(
            onClick = { appViewModel.completeOnboarding() },
            enabled = usageOk && notifOk,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (usageOk && notifOk) "Concluir" else "Conceda as obrigatórias")
        }
    }
}

/** Um cartão de permissão: título, explicação, status e ação. `granted=null` = sem estado (bateria). */
@Composable
private fun PermissionCard(title: String, body: String, granted: Boolean?, action: String, onAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space1)) {
        val status = when (granted) {
            true -> "  ✓ concedido"
            false -> "  • pendente"
            null -> ""
        }
        Text(
            text = title + status,
            style = MaterialTheme.typography.titleMedium,
            color = if (granted == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (granted != true) {
            OutlinedButton(onClick = onAction) { Text(action) }
        }
    }
}
