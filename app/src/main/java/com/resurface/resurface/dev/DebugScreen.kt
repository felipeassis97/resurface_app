package com.resurface.resurface.dev

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resurface.resurface.ui.screens.settings.SettingsScaffold
import com.resurface.resurface.ui.theme.Spacing

/** Tela de Debug (só em build de debug): aviso de teste + controles de onboarding. */
@Composable
fun DebugScreen(onBack: () -> Unit, viewModel: DevToolsViewModel = hiltViewModel()) {
    val alwaysOnboarding by viewModel.alwaysShowOnboarding.collectAsStateWithLifecycle()
    SettingsScaffold(
        title = "Debug",
        onBack = onBack,
        bottomBar = {
            Button(onClick = viewModel::onTestAlert, modifier = Modifier.fillMaxWidth()) {
                Text("Trigger test alert")
            }
            OutlinedButton(onClick = viewModel::onResetOnboarding, modifier = Modifier.fillMaxWidth()) {
                Text("Reset onboarding now")
            }
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.space3),
        ) {
            Text("Always show onboarding", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = alwaysOnboarding, onCheckedChange = viewModel::onToggleAlwaysShowOnboarding)
        }
    }
}
