package com.resurface.resurface.ui.screens.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.resurface.resurface.ble.WristbandConnectionState
import com.resurface.resurface.ui.theme.Spacing

/** Rótulo humano do estado do link da pulseira. */
private fun WristbandConnectionState.label(): String = when (this) {
    is WristbandConnectionState.Connected -> "Pulseira conectada"
    is WristbandConnectionState.Connecting -> "Conectando…"
    WristbandConnectionState.Scanning -> "Procurando pulseira…"
    is WristbandConnectionState.Disconnected -> "Pulseira desconectada"
    is WristbandConnectionState.Failed -> "Falha: ${detail ?: reason.name}"
    WristbandConnectionState.Idle -> "Pulseira não conectada"
}

/**
 * Seção da pulseira em Ajustes (pareamento mínimo + intensidade). Pede permissão BLE
 * just-in-time; a UI rica de scan fica pra depois.
 */
@Composable
fun WristbandSettingsSection(
    state: WristbandConnectionState,
    intensity: Int?,
    onPair: () -> Unit,
    onSetIntensity: (Int?) -> Unit,
) {
    // Pede BLUETOOTH_SCAN + CONNECT; ao conceder, dispara o pareamento.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants -> if (grants.values.all { it }) onPair() }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
        Text("Pulseira", style = MaterialTheme.typography.titleMedium)
        Text(state.label(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
        Button(
            onClick = {
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Procurar e conectar") }

        // Intensidade do pulso (null = auto/padrão do firmware).
        var slider by remember(intensity) { mutableFloatStateOf((intensity ?: DEFAULT_INTENSITY).toFloat()) }
        Text(
            if (intensity == null) "Intensidade: automática" else "Intensidade: ${slider.toInt()}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Slider(
            value = slider,
            onValueChange = { slider = it },
            onValueChangeFinished = { onSetIntensity(slider.toInt()) },
            valueRange = 0f..255f,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = { onSetIntensity(null) }) { Text("Usar intensidade automática") }
    }
}

private const val DEFAULT_INTENSITY = 120
