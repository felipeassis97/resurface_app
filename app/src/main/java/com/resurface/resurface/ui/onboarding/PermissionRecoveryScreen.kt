package com.resurface.resurface.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.permission.AppPermission
import com.resurface.resurface.ui.AppViewModel
import com.resurface.resurface.ui.theme.ResurfaceShapes
import com.resurface.resurface.ui.theme.Spacing

/**
 * Recuperação: o onboarding já foi concluído, mas uma obrigatória foi revogada. Aponta a permissão
 * faltante e leva a concedê-la — sem reabrir o onboarding, sem perfil, sem consentimento. O gate
 * volta pro app sozinho quando tudo estiver concedido (reavaliado no resume).
 */
@Composable
fun PermissionRecoveryScreen(appViewModel: AppViewModel) {
    val statuses by appViewModel.permissionStatuses.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        appViewModel.refresh()
    }

    val missing = AppPermission.required.firstOrNull { statuses[it] != true }

    val (title, body, action, onAction) = when (missing) {
        AppPermission.USAGE_ACCESS -> RecoveryContent(
            title = "Usage access is off",
            body = "This is the counter. Without it Resurface cannot tell how long you spent on short video. Turn it back on to keep working.",
            action = "Open settings",
            onAction = { appViewModel.settingsIntentFor(AppPermission.USAGE_ACCESS)?.let(context::startActivity) },
        )
        AppPermission.NOTIFICATIONS -> RecoveryContent(
            title = "Notifications are off",
            body = "Without them the alert never shows, and the alert is the product. Turn it back on to keep working.",
            action = "Allow",
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
        )
        else -> RecoveryContent("All good", "Permissions are in order.", "Continue") { appViewModel.refresh() }
    }

    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(Spacing.space6),
            verticalArrangement = Arrangement.spacedBy(Spacing.space4, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, ResurfaceShapes.full),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            }
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
            Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(action) }
        }
    }
}

/** Conteúdo textual da recuperação (destructurável). */
private data class RecoveryContent(
    val title: String,
    val body: String,
    val action: String,
    val onAction: () -> Unit,
)
